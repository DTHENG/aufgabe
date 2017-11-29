package com.dtheng.aufgabe.event;

import com.dtheng.aufgabe.io.event.B3F_TactileSwitchInputPressedEvent;
import com.dtheng.aufgabe.io.event.GP2Y0A21YK0F_IrDistanceSensorInputEvent;
import com.google.inject.ImplementedBy;
import com.pploder.events.Event;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(EventManagerImpl.class)
public interface EventManager {

    Event<B3F_TactileSwitchInputPressedEvent> getB3F_TactileSwitchInputPressed();

    Event<GP2Y0A21YK0F_IrDistanceSensorInputEvent> getGP2Y0A21YK0F_IrDistanceSensorInput();
}
