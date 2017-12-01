package com.dtheng.aufgabe.event;

import com.dtheng.aufgabe.input.event.InputCreatedEvent;
import com.dtheng.aufgabe.task.event.TaskCreatedEvent;
import com.dtheng.aufgabe.taskentry.event.B3F_TactileSwitchInputPressedEvent;
import com.dtheng.aufgabe.taskentry.event.GP2Y0A21YK0F_IrDistanceSensorInputEvent;
import com.dtheng.aufgabe.taskentry.event.TaskEntryCreatedEvent;
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

    private final Event<InputCreatedEvent> inputCreatedEvent = new SimpleEvent<>();
    private final Event<TaskEntryCreatedEvent> taskEntryCreatedEvent = new SimpleEvent<>();
    private final Event<TaskCreatedEvent> taskCreatedEvent = new SimpleEvent<>();
    private final Event<B3F_TactileSwitchInputPressedEvent> b3F_tactileSwitchInputPressedEvent = new SimpleEvent<>();
    private final Event<GP2Y0A21YK0F_IrDistanceSensorInputEvent> gp2Y0A21YK0F_irDistanceSensorInputEvent = new SimpleEvent<>();

    @Override
    public Event<InputCreatedEvent> getInputCreated() {
        return inputCreatedEvent;
    }

    @Override
    public Event<TaskEntryCreatedEvent> getTaskEntryCreated() {
        return taskEntryCreatedEvent;
    }

    @Override
    public Event<TaskCreatedEvent> getTaskCreated() {
        return taskCreatedEvent;
    }

    @Override
    public Event<B3F_TactileSwitchInputPressedEvent> getB3F_TactileSwitchInputPressed() {
        return b3F_tactileSwitchInputPressedEvent;
    }

    @Override
    public Event<GP2Y0A21YK0F_IrDistanceSensorInputEvent> getGP2Y0A21YK0F_IrDistanceSensorInput() {
        return gp2Y0A21YK0F_irDistanceSensorInputEvent;
    }
}