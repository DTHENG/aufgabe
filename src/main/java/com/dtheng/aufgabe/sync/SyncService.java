package com.dtheng.aufgabe.sync;

import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.config.model.AufgabeDeviceType;
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
import retrofit.RetrofitError;
import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class SyncService {

    private static final int CRON_INTERVAL_IN_SECONDS = 15;

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

    public Observable<Void> start() {
        return configManager.getConfig()
            .map(AufgabeConfig::getDeviceType)
            .filter(deviceType -> deviceType == AufgabeDeviceType.RASPBERRY_PI)
            .doOnNext(raspberryPi -> {

                /** Task */

                eventManager.getTaskCreated()
                    .addListener((TaskCreatedEvent event) -> taskManager.get(event.getId())
                        .map(AggregateTask::getTask)
                        .flatMap(task -> canSync()
                            .filter(canSync -> canSync)
                            .flatMap(Void -> taskManager.performSync(task)))
                        .onErrorResumeNext(throwable -> {
                            if (throwable instanceof RetrofitError)
                                return Observable.empty();
                            return Observable.error(throwable);
                        })
                        .subscribe(Void -> {}, error -> log.error(error.toString())));

                Observable.interval(CRON_INTERVAL_IN_SECONDS, TimeUnit.SECONDS)
                    .flatMap(Void -> taskSyncCron())
                    .onErrorResumeNext(throwable -> {
                        if (throwable instanceof RetrofitError)
                            return Observable.empty();
                        return Observable.error(throwable);
                    })
                    .subscribe(Void -> {},
                        error -> log.error(error.toString()));

                /** Task Entry */

                eventManager.getTaskEntryCreated()
                    .addListener((TaskEntryCreatedEvent event) -> taskEntryManager.get(event.getId())
                        .flatMap(taskEntry -> canSync()
                            .filter(canSync -> canSync)
                            .flatMap(Void -> taskEntryManager.performSync(taskEntry)))
                        .onErrorResumeNext(throwable -> {
                            if (throwable instanceof RetrofitError)
                                return Observable.empty();
                            return Observable.error(throwable);
                        })
                        .subscribe(Void -> {}, error -> log.error(error.toString())));

                Observable.interval(CRON_INTERVAL_IN_SECONDS, TimeUnit.SECONDS)
                    .flatMap(Void -> taskEntrySyncCron())
                    .onErrorResumeNext(throwable -> {
                        if (throwable instanceof RetrofitError)
                            return Observable.empty();
                        return Observable.error(throwable);
                    })
                    .subscribe(Void -> {},
                        error -> log.error(error.toString()));

                /** Input */

                eventManager.getInputCreated()
                    .addListener((InputCreatedEvent event) -> inputManager.get(event.getId())
                        .flatMap(input -> canSync()
                            .filter(canSync -> canSync)
                            .flatMap(Void -> inputManager.performSync(input)))
                        .onErrorResumeNext(throwable -> {
                            if (throwable instanceof RetrofitError)
                                return Observable.empty();
                            return Observable.error(throwable);
                        })
                        .subscribe(Void -> {}, error -> log.error(error.toString())));

                Observable.interval(CRON_INTERVAL_IN_SECONDS, TimeUnit.SECONDS)
                    .flatMap(Void -> inputSyncCron())
                    .onErrorResumeNext(throwable -> {
                        if (throwable instanceof RetrofitError)
                            return Observable.empty();
                        return Observable.error(throwable);
                    })
                    .subscribe(Void -> {}, error -> log.error(error.toString()));
            })
            .ignoreElements().cast(Void.class);
    }

    private Observable<Void> taskSyncCron() {
        return canSync()
            .filter(canSync -> canSync)
            .flatMap(Void -> {
                TasksRequest tasksRequest = new TasksRequest();
                tasksRequest.setOnlyShowNeedSync(true);
                return taskManager.get(tasksRequest)
                    .map(AggregateTasksResponse::getTasks)
                    .flatMap(tasks -> Observable.from(tasks)
                        .map(AggregateTask::getTask)
                        .flatMap(taskManager::performSync)
                        .ignoreElements().cast(Void.class));
            });
    }

    private Observable<Void> inputSyncCron() {
        return canSync()
            .filter(canSync -> canSync)
            .flatMap(Void -> {
                InputsRequest inputsRequest = new InputsRequest();
                inputsRequest.setOnlyShowNeedSync(true);
                return inputManager.get(inputsRequest)
                    .map(InputsResponse::getInputs)
                    .flatMap(inputs -> Observable.from(inputs)
                        .flatMap(inputManager::performSync))
                    .ignoreElements().cast(Void.class);
            });
    }

    private Observable<Void> taskEntrySyncCron() {
        return canSync()
            .filter(canSync -> canSync)
            .flatMap(Void -> {
                EntriesRequest entriesRequest = new EntriesRequest();
                entriesRequest.setOnlyShowNeedSync(true);
                return taskEntryManager.get(entriesRequest)
                    .map(EntriesResponse::getEntries)
                    .flatMap(entries -> Observable.from(entries)
                        .flatMap(taskEntryManager::performSync)
                        .ignoreElements().cast(Void.class));
            });
    }

    private Observable<Boolean> canSync() {
        return Observable.just(true);
    }
}
