package com.dtheng.aufgabe.input;

import com.dtheng.aufgabe.AufgabeContext;
import com.dtheng.aufgabe.input.dto.*;
import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.Configuration;
import com.dtheng.aufgabe.config.model.DeviceType;
import com.dtheng.aufgabe.device.DeviceManager;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.exceptions.UnsupportedException;
import com.dtheng.aufgabe.util.RandomString;
import com.google.inject.Inject;
import com.pi4j.io.gpio.GpioFactory;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.Date;
import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class InputManagerImpl implements InputManager {

    private InputDAO inputDAO;
    private DeviceManager deviceManager;
    private ConfigManager configManager;
    private AufgabeContext aufgabeContext;

    @Inject
    public InputManagerImpl(InputDAO inputDAO, DeviceManager deviceManager, ConfigManager configManager, AufgabeContext aufgabeContext) {
        this.inputDAO = inputDAO;
        this.deviceManager = deviceManager;
        this.configManager = configManager;
        this.aufgabeContext = aufgabeContext;
    }

    @Override
    public Observable<Void> startUp() {
        return Observable.zip(
            deviceManager.getDeviceId(),
            configManager.getConfig()
                .map(Configuration::getDeviceType),
            (deviceId, deviceType) -> {
                switch (deviceType) {
                    case RASPBERRY_PI:
                        InputsRequest request = new InputsRequest();
                        request.setLimit(29);
                        request.setDevice(Optional.of(deviceId));
                        return get(request)
                            .flatMap(resp -> Observable.from(resp.getInputs())
                                .flatMap(input -> {
                                    InputHandler handler = aufgabeContext.getInjector().getInstance(input.getHandler());
                                    return handler.startUp(input)
                                        .defaultIfEmpty(null);
                                })
                                .toList());
                    default:
                        return Observable.empty();
                }
            })
            .flatMap(o -> o)
            .ignoreElements().cast(Void.class);
    }

    @Override
    public Observable<Input> get(String id) {
        return inputDAO.getInput(id);
    }

    @Override
    public Observable<Input> create(InputCreateRequest request) {
        return Observable.zip(
            deviceManager.getDeviceId(),
            configManager.getConfig().map(Configuration::getDeviceType),
            (deviceId, deviceType) -> {
                if (deviceType != DeviceType.RASPBERRY_PI)
                    throw new UnsupportedException();
                return checkIfIoPinIsFreeThenCreate(deviceId, request);
            })
            .flatMap(o -> o);
    }

    @Override
    public Observable<InputsResponse> get(InputsRequest request) {
        return inputDAO.getInputs(request);
    }

    @Override
    public Observable<Input> remove(String id) {
        return configManager.getConfig()
            .map(Configuration::getDeviceType)
            .flatMap(deviceType -> {
                if (deviceType != DeviceType.RASPBERRY_PI)
                    return Observable.error(new UnsupportedException());
                return get(id);
            })
            .flatMap(input -> inputDAO.removeInput(id)
                .doOnNext(Void -> input.setRemovedAt(Optional.of(new Date())))
                .map(Void -> input));
    }

    private Observable<Input> checkIfIoPinIsFreeThenCreate(String deviceId, InputCreateRequest request) {
        InputsRequest existingInputRequest = new InputsRequest();
        existingInputRequest.setDevice(Optional.of(deviceId));
        existingInputRequest.setIoPin(Optional.of(request.getIoPin()));
        return inputDAO.getInputs(existingInputRequest)
            .map(InputsResponse::getTotal)
            .flatMap(numberOfExistingInputs -> {
                if (numberOfExistingInputs > 0)
                    return Observable.error(new AufgabeException("Input already exists for IO Pin "+ request.getIoPin()));
                Input input = new Input();
                input.setId("input-"+ new RandomString(8).nextString());
                input.setCreatedAt(request.getCreatedAt().isPresent() ? request.getCreatedAt().get() : new Date());
                input.setIoPin(request.getIoPin());
                input.setTaskId(request.getTaskId());
                input.setDevice(deviceId);
                try {
                    Class rawClass = Class.forName(request.getHandler());
                    try {
                        if (! (rawClass.newInstance() instanceof InputHandler))
                            return Observable.error(new AufgabeException("\""+ request.getHandler() +"\" is not a valid \"handler\""));
                        Class<? extends InputHandler> handler = (Class<? extends InputHandler>) rawClass;
                        input.setHandler(handler);
                        return inputDAO.createInput(input);
                    } catch (Exception e) {
                        log.error("Got error attempting .newInstance() on class {}", rawClass);
                        return Observable.error(new AufgabeException("\""+ request.getHandler() +"\" is not a valid \"handler\""));
                    }
                } catch (ClassNotFoundException cnfe) {
                    return Observable.error(new AufgabeException("\""+ request.getHandler() +"\" is not a valid \"handler\""));
                }
            });
    }
}