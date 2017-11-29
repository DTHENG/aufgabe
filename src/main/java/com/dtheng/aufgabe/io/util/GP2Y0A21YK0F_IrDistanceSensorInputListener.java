package com.dtheng.aufgabe.io.util;

import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.io.event.GP2Y0A21YK0F_IrDistanceSensorInputEvent;
import com.google.inject.Inject;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import lombok.Setter;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class GP2Y0A21YK0F_IrDistanceSensorInputListener implements GpioPinListenerDigital {

    @Setter
    private String inputId;

    private EventManager eventManager;

    @Inject
    public GP2Y0A21YK0F_IrDistanceSensorInputListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        eventManager.getGP2Y0A21YK0F_IrDistanceSensorInput()
            .trigger(new GP2Y0A21YK0F_IrDistanceSensorInputEvent(inputId, event.getState() == PinState.HIGH));
    }
}
