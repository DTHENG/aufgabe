package com.dtheng.aufgabe.event;

import com.dtheng.aufgabe.io.event.B3F_TactileSwitchInputPressedEvent;
import com.dtheng.aufgabe.io.event.GP2Y0A21YK0F_IrDistanceSensorInputEvent;
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

    private final Event<B3F_TactileSwitchInputPressedEvent> b3F_tactileSwitchInputPressedEventEvent = new SimpleEvent<>();
    private final Event<GP2Y0A21YK0F_IrDistanceSensorInputEvent> gp2Y0A21YK0F_irDistanceSensorInputEventEvent = new SimpleEvent<>();

    @Override
    public Event<B3F_TactileSwitchInputPressedEvent> getB3F_TactileSwitchInputPressed() {
        return b3F_tactileSwitchInputPressedEventEvent;
    }

    @Override
    public Event<GP2Y0A21YK0F_IrDistanceSensorInputEvent> getGP2Y0A21YK0F_IrDistanceSensorInput() {
        return gp2Y0A21YK0F_irDistanceSensorInputEventEvent;
    }
}