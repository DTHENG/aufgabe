package com.dtheng.aufgabe.input;

import com.dtheng.aufgabe.AufgabeContext;
import com.dtheng.aufgabe.AufgabeService;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.device.DeviceService;
import com.dtheng.aufgabe.input.dto.InputsRequest;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class InputService implements AufgabeService {

    private ConfigManager configManager;
    private InputManager inputManager;
    private DeviceService deviceService;
    private AufgabeContext aufgabeContext;

    @Inject
    public InputService(ConfigManager configManager, InputManager inputManager, DeviceService deviceService, AufgabeContext aufgabeContext) {
        this.configManager = configManager;
        this.inputManager = inputManager;
        this.deviceService = deviceService;
        this.aufgabeContext = aufgabeContext;
    }

    @Override
    public Observable<Map<String, Object>> startUp() {
        return Observable.zip(
            deviceService.getDeviceId(),
            configManager.getConfig()
                .map(AufgabeConfig::getDeviceType),
            (deviceId, deviceType) -> {
                switch (deviceType) {
                    case RASPBERRY_PI:
                        InputsRequest request = new InputsRequest();
                        request.setLimit(29);
                        request.setDeviceId(Optional.of(deviceId));
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
            .map(Void -> new HashMap<>());
    }

    @Override
    public long order() {
        return 1511942400;
    }
}