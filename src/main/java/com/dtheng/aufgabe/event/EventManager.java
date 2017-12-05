package com.dtheng.aufgabe.event;

import com.dtheng.aufgabe.device.event.DeviceCreatedEvent;
import com.dtheng.aufgabe.input.event.InputCreatedEvent;
import com.dtheng.aufgabe.task.event.TaskCreatedEvent;
import com.dtheng.aufgabe.task.event.TaskUpdatedEvent;
import com.dtheng.aufgabe.taskentry.event.B3F_TactileSwitchInputPressedEvent;
import com.dtheng.aufgabe.taskentry.event.GP2Y0A21YK0F_IrDistanceSensorInputEvent;
import com.dtheng.aufgabe.taskentry.event.TaskEntryCreatedEvent;
import com.google.inject.ImplementedBy;
import com.pploder.events.Event;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(EventManagerImpl.class)
public interface EventManager {

    Event<InputCreatedEvent> getInputCreated();

    Event<TaskEntryCreatedEvent> getTaskEntryCreated();

    Event<TaskCreatedEvent> getTaskCreated();

    Event<TaskUpdatedEvent> getTaskUpdated();

    Event<DeviceCreatedEvent> getDeviceCreated();

    Event<B3F_TactileSwitchInputPressedEvent> getB3F_TactileSwitchInputPressed();

    Event<GP2Y0A21YK0F_IrDistanceSensorInputEvent> getGP2Y0A21YK0F_IrDistanceSensorInput();
}
