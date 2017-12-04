package com.dtheng.aufgabe.io.util;

import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.taskentry.event.B3F_TactileSwitchInputPressedEvent;
import com.google.inject.Inject;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class B3F_TactileSwitchInputListener extends BaseGpioPinListenerDigital {

    @Setter
    private String inputId;

    private EventManager eventManager;

    @Inject
    public B3F_TactileSwitchInputListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public Observable<Void> startUp() {
        return Observable.empty();
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (event.getState() == PinState.HIGH)
            eventManager.getB3F_TactileSwitchInputPressed()
                .trigger(new B3F_TactileSwitchInputPressedEvent(inputId));
    }
}
