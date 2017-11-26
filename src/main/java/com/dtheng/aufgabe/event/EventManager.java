package com.dtheng.aufgabe.event;

import com.dtheng.aufgabe.io.event.ButtonPressedEvent;
import com.google.inject.ImplementedBy;
import com.pploder.events.Event;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(EventManagerImpl.class)
public interface EventManager {

    Event<ButtonPressedEvent> getButtonPressed();
}
