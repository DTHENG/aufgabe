package com.dtheng.aufgabe.io.util;

import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.io.event.B3F_TactileSwitchInputPressedEvent;
import com.google.inject.Inject;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import lombok.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class B3F_TactileSwitchInputListener implements GpioPinListenerDigital {

    @Setter
    private String inputId;

    private EventManager eventManager;

    @Inject
    public B3F_TactileSwitchInputListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (event.getState() == PinState.HIGH)
            eventManager.getB3F_TactileSwitchInputPressed()
                .trigger(new B3F_TactileSwitchInputPressedEvent(inputId));
    }
}
