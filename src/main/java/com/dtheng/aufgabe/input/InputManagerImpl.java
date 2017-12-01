package com.dtheng.aufgabe.input;

import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.input.dto.*;
import com.dtheng.aufgabe.input.event.InputCreatedEvent;
import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.config.model.AufgabeDeviceType;
import com.dtheng.aufgabe.device.DeviceManager;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.exceptions.UnsupportedException;
import com.dtheng.aufgabe.sync.SyncManager;
import com.dtheng.aufgabe.util.RandomString;
import com.google.inject.Inject;
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
    private SyncManager syncManager;
    private EventManager eventManager;

    @Inject
    public InputManagerImpl(InputDAO inputDAO, DeviceManager deviceManager, ConfigManager configManager,
                            SyncManager syncManager, EventManager eventManager) {
        this.inputDAO = inputDAO;
        this.deviceManager = deviceManager;
        this.configManager = configManager;
        this.syncManager = syncManager;
        this.eventManager = eventManager;
    }

    @Override
    public Observable<Input> get(String id) {
        return inputDAO.getInput(id);
    }

    @Override
    public Observable<Input> create(InputCreateRequest request) {
        return deviceManager.getDeviceId()
            .flatMap(deviceId -> configManager.getConfig().map(AufgabeConfig::getDeviceType)
                .flatMap(deviceType -> {
                    switch (deviceType) {
                        case RASPBERRY_PI:
                            return checkIfIoPinIsFreeThenCreate(deviceId, request);
                        case EC2_INSTANCE:
                            try {
                                Class rawClass = Class.forName(request.getHandler());
                                try {
                                    if (! (rawClass.newInstance() instanceof InputHandler))
                                        return Observable.error(new AufgabeException("\""+ request.getHandler() +"\" is not a valid \"handler\""));
                                    Class<? extends InputHandler> handler = (Class<? extends InputHandler>) rawClass;
                                    return inputDAO.createInput(new Input(request.getId().get(), request.getCreatedAt().get(), request.getIoPin(), request.getTaskId(), request.getDevice().get(), Optional.empty(), handler, Optional.empty(), Optional.empty()));
                                } catch (Exception e) {
                                    log.error("Got error attempting .newInstance() on class {}", rawClass);
                                    return Observable.error(new AufgabeException("\""+ request.getHandler() +"\" is not a valid \"handler\""));
                                }
                            } catch (ClassNotFoundException cnfe) {
                                return Observable.error(new AufgabeException("\""+ request.getHandler() +"\" is not a valid \"handler\""));
                            }
                        default:
                            return Observable.error(new UnsupportedException());
                    }
                })
                .doOnNext(input -> eventManager.getInputCreated().trigger(new InputCreatedEvent(input.getId()))));
    }

    @Override
    public Observable<InputsResponse> get(InputsRequest request) {
        return inputDAO.getInputs(request);
    }

    @Override
    public Observable<Input> remove(String id) {
        return configManager.getConfig()
            .map(AufgabeConfig::getDeviceType)
            .flatMap(deviceType -> {
                if (deviceType != AufgabeDeviceType.RASPBERRY_PI)
                    return Observable.error(new UnsupportedException());
                return get(id);
            })
            .flatMap(input -> inputDAO.removeInput(id)
                .doOnNext(Void -> input.setRemovedAt(Optional.of(new Date())))
                .map(Void -> input));
    }

    @Override
    public Observable<Input> performSync(Input input) {
        return syncManager.getSyncClient()
            .flatMap(syncClient -> syncClient.syncInput(new InputSyncRequest(input.getId(), input.getCreatedAt().getTime(), input.getIoPin(), input.getTaskId(), input.getDevice(), input.getHandler().getCanonicalName()))
                .defaultIfEmpty(null)
                .flatMap(Void -> inputDAO.setSyncedAt(input.getId(), new Date())));
    }

    @Override
    public Observable<String> getDevices() {
        return inputDAO.getDevices();
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