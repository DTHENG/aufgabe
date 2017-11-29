package com.dtheng.aufgabe.taskentry;

import com.dtheng.aufgabe.AufgabeContext;
import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.io.event.B3F_TactileSwitchInputPressedEvent;
import com.dtheng.aufgabe.task.TaskManager;
import com.dtheng.aufgabe.taskentry.dto.TaskEntryCreateRequest;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.function.Consumer;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class TaskEntryService {

    private EventManager eventManager;
    private AufgabeContext aufgabeContext;

    @Inject
    public TaskEntryService(EventManager eventManager, AufgabeContext aufgabeContext) {
        this.eventManager = eventManager;
        this.aufgabeContext = aufgabeContext;
    }

    public Observable<Void> startUp() {
        eventManager.getB3F_TactileSwitchInputPressed().addListener(aufgabeContext.getInjector().getInstance(Handlers.ButtonPressed.class));
        return Observable.empty();
    }

    private static class Handlers {

        public static class ButtonPressed implements Consumer<B3F_TactileSwitchInputPressedEvent> {

            private InputManager inputManager;
            private TaskEntryManager taskEntryManager;
            private TaskManager taskManager;

            @Inject
            public ButtonPressed(InputManager inputManager, TaskEntryManager taskEntryManager, TaskManager taskManager) {
                this.inputManager = inputManager;
                this.taskEntryManager = taskEntryManager;
                this.taskManager = taskManager;
            }

            @Override
            public void accept(B3F_TactileSwitchInputPressedEvent event) {
                inputManager.get(event.getInputId())
                    .flatMap(input -> taskEntryManager.create(new TaskEntryCreateRequest(input.getTaskId(), input.getId())))
                    .flatMap(taskEntry -> taskManager.get(taskEntry.getTaskId()))
                    .subscribe(Void -> {},
                        error -> log.error(error.toString()));
            }
        }
    }
}
