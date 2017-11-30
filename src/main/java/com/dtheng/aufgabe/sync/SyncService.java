package com.dtheng.aufgabe.sync;

import com.dtheng.aufgabe.AufgabeService;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.Configuration;
import com.dtheng.aufgabe.config.model.DeviceType;
import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.input.dto.InputsRequest;
import com.dtheng.aufgabe.input.dto.InputsResponse;
import com.dtheng.aufgabe.input.event.InputCreatedEvent;
import com.dtheng.aufgabe.task.TaskManager;
import com.dtheng.aufgabe.task.dto.AggregateTask;
import com.dtheng.aufgabe.task.dto.AggregateTasksResponse;
import com.dtheng.aufgabe.task.dto.TasksRequest;
import com.dtheng.aufgabe.task.event.TaskCreatedEvent;
import com.dtheng.aufgabe.taskentry.TaskEntryManager;
import com.dtheng.aufgabe.taskentry.dto.EntriesRequest;
import com.dtheng.aufgabe.taskentry.dto.EntriesResponse;
import com.dtheng.aufgabe.taskentry.event.TaskEntryCreatedEvent;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class SyncService implements AufgabeService {

    private EventManager eventManager;
    private InputManager inputManager;
    private TaskManager taskManager;
    private TaskEntryManager taskEntryManager;
    private ConfigManager configManager;

    @Inject
    public SyncService(EventManager eventManager, InputManager inputManager, TaskManager taskManager, TaskEntryManager taskEntryManager,
                       ConfigManager configManager) {
        this.eventManager = eventManager;
        this.inputManager = inputManager;
        this.taskManager = taskManager;
        this.taskEntryManager = taskEntryManager;
        this.configManager = configManager;
    }

    @Override
    public Observable<Void> startUp() {
        return configManager.getConfig()
            .map(Configuration::getDeviceType)
            .filter(deviceType -> deviceType == DeviceType.RASPBERRY_PI)
            .doOnNext(raspberryPi -> {

                /** Task */

                eventManager.getTaskCreated()
                    .addListener((TaskCreatedEvent event) -> taskManager.get(event.getId())
                        .map(AggregateTask::getTask)
                        .flatMap(taskManager::performSync)
                        .subscribe(Void -> {}, error -> log.error(error.toString())));

                Observable.interval(3, TimeUnit.MINUTES)
                    .flatMap(Void -> taskSyncCron())
                    .subscribe(Void -> {},
                        error -> log.error(error.toString()));

                /** Task Entry */

                eventManager.getTaskEntryCreated()
                    .addListener((TaskEntryCreatedEvent event) -> taskEntryManager.get(event.getId())
                        .flatMap(taskEntryManager::performSync)
                        .subscribe(Void -> {}, error -> log.error(error.toString())));

                Observable.interval(3, TimeUnit.MINUTES)
                    .flatMap(Void -> taskEntrySyncCron())
                    .subscribe(Void -> {},
                        error -> log.error(error.toString()));

                /** Input */

                eventManager.getInputCreated()
                    .addListener((InputCreatedEvent event) -> inputManager.get(event.getId())
                        .flatMap(inputManager::performSync)
                        .subscribe(Void -> {}, error -> log.error(error.toString())));

                Observable.interval(3, TimeUnit.MINUTES)
                    .flatMap(Void -> inputSyncCron())
                    .subscribe(Void -> {}, error -> log.error(error.toString()));
            })
            .ignoreElements().cast(Void.class);
    }

    private Observable<Void> taskSyncCron() {
        TasksRequest tasksRequest = new TasksRequest();
        tasksRequest.setOnlyShowNeedSync(true);
        return taskManager.get(tasksRequest)
            .map(AggregateTasksResponse::getTasks)
            .flatMap(tasks -> Observable.from(tasks)
                .map(AggregateTask::getTask)
                .flatMap(taskManager::performSync)
                .ignoreElements().cast(Void.class));
    }

    private Observable<Void> inputSyncCron() {
        InputsRequest inputsRequest = new InputsRequest();
        inputsRequest.setOnlyShowNeedSync(true);
        return inputManager.get(inputsRequest)
            .map(InputsResponse::getInputs)
            .flatMap(inputs -> Observable.from(inputs)
                .flatMap(inputManager::performSync))
            .ignoreElements().cast(Void.class);
    }

    private Observable<Void> taskEntrySyncCron() {
        EntriesRequest entriesRequest = new EntriesRequest();
        entriesRequest.setOnlyShowNeedSync(true);
        return taskEntryManager.get(entriesRequest)
            .map(EntriesResponse::getEntries)
            .flatMap(entries -> Observable.from(entries)
                .flatMap(taskEntryManager::performSync)
                .ignoreElements().cast(Void.class));
    }

}
