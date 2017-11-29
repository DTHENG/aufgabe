package com.dtheng.aufgabe.input;

import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.DeviceType;
import com.dtheng.aufgabe.input.dto.InputsRequest;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class InputService {

    private ConfigManager configManager;
    private InputManager inputManager;

    @Inject
    public InputService(ConfigManager configManager, InputManager inputManager) {
        this.configManager = configManager;
        this.inputManager = inputManager;
    }

    public Observable<Void> startUp() {
        Observable.interval(3, TimeUnit.MINUTES)
            .flatMap(Void -> inputSyncCron())
            .subscribe(Void -> {},
                error -> log.error(error.toString()));

        return Observable.empty();
    }

    private Observable<Void> inputSyncCron() {
        return Observable.defer(() -> configManager.getConfig()
            .filter(configuration -> configuration.getDeviceType() == DeviceType.RASPBERRY_PI)
            .flatMap(Void -> {
                InputsRequest inputsRequest = new InputsRequest();
                inputsRequest.setOnlyShowNeedSync(true);
                return inputManager.get(inputsRequest)
                    .flatMap(inputsResponse -> Observable.from(inputsResponse.getInputs())
                        .flatMap(inputManager::performSyncRequest));
            })
            .ignoreElements().cast(Void.class));
    }

}
