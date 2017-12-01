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
    public Observable<Void> startUp() {
        return configManager.getConfig()
            .map(AufgabeConfig::getDeviceType)
            .filter(deviceType -> deviceType == AufgabeDeviceType.RASPBERRY_PI)
            .doOnNext(Void -> controller = Optional.of(GpioFactory.getInstance()))
            .ignoreElements().cast(Void.class);
    }

    public Observable<GpioController> getController() {
        if (controller.isPresent())
            return Observable.just(controller.get());
        return Observable.empty();
    }
}
