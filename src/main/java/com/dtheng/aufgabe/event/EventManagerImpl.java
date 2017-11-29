package com.dtheng.aufgabe.event;

import com.dtheng.aufgabe.io.event.B3F_TactileSwitchInputPressedEvent;
import com.google.inject.Singleton;
import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@Singleton
public class EventManagerImpl implements EventManager {

    private final Event<B3F_TactileSwitchInputPressedEvent> buttonPressed = new SimpleEvent<>();

    public Event<B3F_TactileSwitchInputPressedEvent> getButtonPressed() {
        return buttonPressed;
    }
}