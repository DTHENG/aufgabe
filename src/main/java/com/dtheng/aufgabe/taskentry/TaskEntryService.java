package com.dtheng.aufgabe.taskentry;

import com.dtheng.aufgabe.AufgabeContext;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.DeviceType;
import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.io.event.B3F_TactileSwitchInputPressedEvent;
import com.dtheng.aufgabe.io.event.GP2Y0A21YK0F_IrDistanceSensorInputEvent;
import com.dtheng.aufgabe.task.TaskManager;
import com.dtheng.aufgabe.task.dto.AggregateTask;
import com.dtheng.aufgabe.task.dto.TasksRequest;
import com.dtheng.aufgabe.taskentry.dto.EntriesRequest;
import com.dtheng.aufgabe.taskentry.dto.TaskEntryCreateRequest;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class TaskEntryService {

    private EventManager eventManager;
    private AufgabeContext aufgabeContext;
    private ConfigManager configManager;
    private TaskEntryManager taskEntryManager;

    @Inject
    public TaskEntryService(EventManager eventManager, AufgabeContext aufgabeContext, ConfigManager configManager, TaskEntryManager taskEntryManager) {
        this.eventManager = eventManager;
        this.aufgabeContext = aufgabeContext;
        this.configManager = configManager;
        this.taskEntryManager = taskEntryManager;
    }

    public Observable<Void> startUp() {
        eventManager.getB3F_TactileSwitchInputPressed().addListener(aufgabeContext.getInjector().getInstance(Handlers.ButtonPressed.class));
        eventManager.getGP2Y0A21YK0F_IrDistanceSensorInput().addListener(aufgabeContext.getInjector().getInstance(Handlers.IrSensorData.class));

        Observable.interval(3, TimeUnit.MINUTES)
            .flatMap(Void -> taskEntrySyncCron())
            .subscribe(Void -> {},
                error -> log.error(error.toString()));

        return Observable.empty();
    }

    private Observable<Void> taskEntrySyncCron() {
        return Observable.defer(() -> configManager.getConfig()
            .filter(configuration -> configuration.getDeviceType() == DeviceType.RASPBERRY_PI)
            .flatMap(Void -> {
                EntriesRequest entriesRequest = new EntriesRequest();
                entriesRequest.setOnlyShowNeedSync(true);
                return taskEntryManager.get(entriesRequest);
            })
            .flatMap(entriesResponse -> Observable.from(entriesResponse.getEntries())
                .flatMap(taskEntryManager::performSyncRequest)
                .ignoreElements().cast(Void.class)));
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
                    .flatMap(input -> taskEntryManager.create(new TaskEntryCreateRequest(input.getTaskId(), input.getId(), Optional.empty(), Optional.empty())))
                    .flatMap(taskEntry -> taskManager.get(taskEntry.getTaskId()))
                    .subscribe(Void -> {},
                        error -> log.error(error.toString()));
            }
        }

        public static class IrSensorData implements Consumer<GP2Y0A21YK0F_IrDistanceSensorInputEvent> {

            @Override
            public void accept(GP2Y0A21YK0F_IrDistanceSensorInputEvent event) {
                log.info("event! {}", event);
            }
        }
    }
}
