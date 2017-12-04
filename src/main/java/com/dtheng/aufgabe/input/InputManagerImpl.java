package com.dtheng.aufgabe.input;

import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.input.dto.*;
import com.dtheng.aufgabe.input.event.InputCreatedEvent;
import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.device.DeviceService;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.exceptions.UnsupportedException;
import com.dtheng.aufgabe.security.SecurityManager;
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
    private DeviceService deviceService;
    private ConfigManager configManager;
    private SyncManager syncManager;
    private EventManager eventManager;
    private SecurityManager securityManager;

    @Inject
    public InputManagerImpl(InputDAO inputDAO, DeviceService deviceService, ConfigManager configManager,
                            SyncManager syncManager, EventManager eventManager, SecurityManager securityManager) {
        this.inputDAO = inputDAO;
        this.deviceService = deviceService;
        this.configManager = configManager;
        this.syncManager = syncManager;
        this.eventManager = eventManager;
        this.securityManager = securityManager;
    }

    @Override
    public Observable<Input> get(String id) {
        return inputDAO.getInput(id);
    }

    @Override
    public Observable<Input> create(InputCreateRequest request) {
        return deviceService.getDeviceId()
            .flatMap(deviceId -> configManager.getConfig().map(AufgabeConfig::getDeviceType)
                .flatMap(deviceType -> {
                    switch (deviceType) {
                        case RASPBERRY_PI:
                            return checkIfIoPinIsFreeThenCreate(deviceId, request);
                        case MAC_OS:
                        case EC2_INSTANCE:
                            try {
                                Class rawClass = Class.forName(request.getHandler());
                                try {
                                    if (! (rawClass.newInstance() instanceof InputHandler))
                                        return Observable.error(new AufgabeException("\""+ request.getHandler() +"\" is not a valid \"handler\""));
                                    Class<? extends InputHandler> handler = (Class<? extends InputHandler>) rawClass;
                                    return inputDAO.createInput(
                                        new Input(
                                            request.getId().orElseGet(() -> "input-"+ new RandomString(8).nextString()),
                                            request.getCreatedAt().orElseGet(Date::new),
                                            request.getIoPin(),
                                            request.getTaskId(),
                                            request.getDevice().orElseGet(() -> deviceId),
                                            Optional.empty(),
                                            handler,
                                            Optional.empty(),
                                            Optional.empty()));
                                } catch (Exception e) {
                                    return Observable.error(e);
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
                switch (deviceType) {
                    case MAC_OS:
                    case RASPBERRY_PI:
                        return get(id);
                    default:
                        return Observable.error(new UnsupportedException());
                }
            })
            .flatMap(input -> inputDAO.removeInput(id)
                .defaultIfEmpty(null)
                .doOnNext(Void -> input.setRemovedAt(Optional.of(new Date())))
                .map(Void -> input));
    }

    @Override
    public Observable<Input> performSync(Input input) {
        InputSyncRequest request = new InputSyncRequest(input.getId(), input.getCreatedAt().getTime(), input.getIoPin(), input.getTaskId(), input.getDeviceId(), input.getHandler().getCanonicalName());
        return Observable.zip(
            configManager.getConfig()
                .map(AufgabeConfig::getPublicKey),
            securityManager.getSignature(request),
            syncManager.getSyncClient(),
            (publicKey, signature, syncClient) ->
                syncClient.syncInput(publicKey, signature, request))
            .flatMap(o -> o)
            .defaultIfEmpty(null)
            .flatMap(Void -> inputDAO.setSyncedAt(input.getId(), new Date()));
    }

    private Observable<Input> checkIfIoPinIsFreeThenCreate(String deviceId, InputCreateRequest request) {
        InputsRequest existingInputRequest = new InputsRequest();
        existingInputRequest.setDeviceId(Optional.of(deviceId));
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
                input.setDeviceId(deviceId);
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