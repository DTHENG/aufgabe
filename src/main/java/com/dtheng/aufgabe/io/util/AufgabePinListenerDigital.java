package com.dtheng.aufgabe.io.util;

import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.io.event.ButtonPressedEvent;
import com.google.inject.Inject;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import lombok.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@AllArgsConstructor
public class AufgabePinListenerDigital implements GpioPinListenerDigital {

    @Setter
    private String buttonId;

    private EventManager eventManager;

    @Inject
    public AufgabePinListenerDigital(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (event.getState() == PinState.HIGH)
            eventManager.getButtonPressed().trigger(new ButtonPressedEvent(buttonId));
    }
}
