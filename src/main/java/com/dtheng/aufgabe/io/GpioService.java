package com.dtheng.aufgabe.io;

import com.dtheng.aufgabe.AufgabeService;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.config.model.AufgabeDeviceType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@Singleton
public class GpioService implements AufgabeService {

    private Optional<GpioController> controller = Optional.empty();

    private ConfigManager configManager;

    @Inject
    public GpioService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public Observable<Map<String, Object>> startUp() {
        return configManager.getConfig()
            .map(AufgabeConfig::getDeviceType)
            .map(deviceType -> deviceType == AufgabeDeviceType.RASPBERRY_PI)
            .map(isRaspberryPi -> {
                if (isRaspberryPi)
                    controller = Optional.of(GpioFactory.getInstance());
                Map<String, Object> metaData = new HashMap<>();
                metaData.put("gpioEnabled", isRaspberryPi);
                return metaData;
            });
    }

    @Override
    public long order() {
        return 1507076400;
    }

    Observable<GpioController> getController() {
        if (controller.isPresent())
            return Observable.just(controller.get());
        return Observable.empty();
    }
}
