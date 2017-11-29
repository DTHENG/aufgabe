package com.dtheng.aufgabe.taskentry;

import com.dtheng.aufgabe.AufgabeContext;
import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.io.event.ButtonPressedEvent;
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
        eventManager.getButtonPressed().addListener(aufgabeContext.getInjector().getInstance(Handlers.ButtonPressed.class));
        return Observable.empty();
    }

    private static class Handlers {

        public static class ButtonPressed implements Consumer<ButtonPressedEvent> {

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
            public void accept(ButtonPressedEvent buttonPressedEvent) {
                inputManager.get(buttonPressedEvent.getInputId())
                    .flatMap(input -> taskEntryManager.create(new TaskEntryCreateRequest(input.getTaskId())))
                    .flatMap(taskEntry -> taskManager.get(taskEntry.getTaskId()))
                    .subscribe(Void -> {},
                        error -> log.error(error.toString()));
            }
        }
    }
}
