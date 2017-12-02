package com.dtheng.aufgabe.taskentry;

import com.dtheng.aufgabe.AufgabeContext;
import com.dtheng.aufgabe.AufgabeService;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.taskentry.event.B3F_TactileSwitchInputPressedEvent;
import com.dtheng.aufgabe.taskentry.event.GP2Y0A21YK0F_IrDistanceSensorInputEvent;
import com.dtheng.aufgabe.taskentry.dto.TaskEntryCreateRequest;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class TaskEntryService implements AufgabeService {

    private EventManager eventManager;
    private AufgabeContext aufgabeContext;

    @Inject
    public TaskEntryService(EventManager eventManager, AufgabeContext aufgabeContext) {
        this.eventManager = eventManager;
        this.aufgabeContext = aufgabeContext;
    }

    @Override
    public Observable<Map<String, Object>> startUp() {
        eventManager.getB3F_TactileSwitchInputPressed().addListener(aufgabeContext.getInjector().getInstance(Handlers.ButtonPressed.class));
        eventManager.getGP2Y0A21YK0F_IrDistanceSensorInput().addListener(aufgabeContext.getInjector().getInstance(Handlers.IrSensorData.class));

        return Observable.empty();
    }

    @Override
    public long order() {
        return 1511683200;
    }

    private static class Handlers {

        public static class ButtonPressed implements Consumer<B3F_TactileSwitchInputPressedEvent> {

            private InputManager inputManager;
            private TaskEntryManager taskEntryManager;

            @Inject
            public ButtonPressed(InputManager inputManager, TaskEntryManager taskEntryManager) {
                this.inputManager = inputManager;
                this.taskEntryManager = taskEntryManager;
            }

            @Override
            public void accept(B3F_TactileSwitchInputPressedEvent event) {
                inputManager.get(event.getInputId())
                    .flatMap(input -> taskEntryManager.create(new TaskEntryCreateRequest(input.getTaskId(), input.getId(), Optional.empty(), Optional.empty())))
                    .subscribe(Void -> {},
                        error -> log.error(error.toString()));
            }
        }

        public static class IrSensorData implements Consumer<GP2Y0A21YK0F_IrDistanceSensorInputEvent> {

            private InputManager inputManager;
            private TaskEntryManager taskEntryManager;

            @Inject
            public IrSensorData(InputManager inputManager, TaskEntryManager taskEntryManager) {
                this.inputManager = inputManager;
                this.taskEntryManager = taskEntryManager;
            }

            @Override
            public void accept(GP2Y0A21YK0F_IrDistanceSensorInputEvent event) {
                inputManager.get(event.getInputId())
                    .flatMap(input -> taskEntryManager.create(new TaskEntryCreateRequest(input.getTaskId(), input.getId(), Optional.empty(), Optional.of(event.getLastMovement()))))
                    .subscribe(Void -> {},
                        error -> log.error(error.toString()));
            }
        }
    }
}
