package com.dtheng.aufgabe.input;

import com.dtheng.aufgabe.AufgabeContext;
import com.dtheng.aufgabe.AufgabeService;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.Configuration;
import com.dtheng.aufgabe.device.DeviceManager;
import com.dtheng.aufgabe.input.dto.InputsRequest;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class InputService implements AufgabeService {

    private ConfigManager configManager;
    private InputManager inputManager;
    private DeviceManager deviceManager;
    private AufgabeContext aufgabeContext;

    @Inject
    public InputService(ConfigManager configManager, InputManager inputManager, DeviceManager deviceManager, AufgabeContext aufgabeContext) {
        this.configManager = configManager;
        this.inputManager = inputManager;
        this.deviceManager = deviceManager;
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
                        return inputManager.get(request)
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
}