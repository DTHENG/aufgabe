package com.dtheng.aufgabe.sync;

import com.dtheng.aufgabe.AufgabeService;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.config.model.AufgabeDeviceType;
import com.dtheng.aufgabe.device.DeviceManager;
import com.dtheng.aufgabe.device.dto.AggregateDevice;
import com.dtheng.aufgabe.device.dto.DevicesRequest;
import com.dtheng.aufgabe.device.dto.DevicesResponse;
import com.dtheng.aufgabe.device.event.DeviceCreatedEvent;
import com.dtheng.aufgabe.device.event.DeviceUpdatedEvent;
import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.input.dto.InputsRequest;
import com.dtheng.aufgabe.input.dto.InputsResponse;
import com.dtheng.aufgabe.input.event.InputCreatedEvent;
import com.dtheng.aufgabe.task.TaskManager;
import com.dtheng.aufgabe.task.dto.TasksRequest;
import com.dtheng.aufgabe.task.dto.TasksResponse;
import com.dtheng.aufgabe.task.event.TaskCreatedEvent;
import com.dtheng.aufgabe.task.event.TaskUpdatedEvent;
import com.dtheng.aufgabe.taskentry.TaskEntryManager;
import com.dtheng.aufgabe.taskentry.dto.EntriesRequest;
import com.dtheng.aufgabe.taskentry.dto.EntriesResponse;
import com.dtheng.aufgabe.taskentry.event.TaskEntryCreatedEvent;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import retrofit.RetrofitError;
import rx.Observable;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class SyncService implements AufgabeService {

    private static final int CRON_INTERVAL_IN_SECONDS = 15;

    private EventManager eventManager;
    private InputManager inputManager;
    private TaskManager taskManager;
    private TaskEntryManager taskEntryManager;
    private ConfigManager configManager;
    private DeviceManager deviceManager;

    @Inject
    public SyncService(EventManager eventManager, InputManager inputManager, TaskManager taskManager, TaskEntryManager taskEntryManager,
                       ConfigManager configManager, DeviceManager deviceManager) {
        this.eventManager = eventManager;
        this.inputManager = inputManager;
        this.taskManager = taskManager;
        this.taskEntryManager = taskEntryManager;
        this.configManager = configManager;
        this.deviceManager = deviceManager;
    }

    @Override
    public Observable<Map<String, Object>> startUp() {
        return configManager.getConfig()
            .map(AufgabeConfig::getDeviceType)
            .filter(deviceType -> deviceType == AufgabeDeviceType.RASPBERRY_PI)
            .flatMap(raspberryPi -> {

                /** Task */

                eventManager.getTaskCreated()
                    .addListener((TaskCreatedEvent event) -> taskManager.get(event.getId())
                        .flatMap(task -> canSync()
                            .filter(canSync -> canSync)
                            .flatMap(Void -> taskManager.performSync(task)))
                        .onErrorResumeNext(throwable -> {
                            if (throwable instanceof RetrofitError)
                                return Observable.empty();
                            return Observable.error(throwable);
                        })
                        .subscribe(Void -> {}, error -> log.error(error.toString())));

                eventManager.getTaskUpdated()
                    .addListener((TaskUpdatedEvent event) -> taskManager.get(event.getId())
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
                        error -> {
                            log.error(error.toString());
                            error.printStackTrace();
                        });

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

                /** Device */

                eventManager.getDeviceCreated()
                    .addListener((DeviceCreatedEvent event) -> deviceManager.get(event.getId())
                        .flatMap(device -> canSync()
                            .filter(canSync -> canSync)
                            .flatMap(Void -> deviceManager.performSync(device)))
                        .onErrorResumeNext(throwable -> {
                            if (throwable instanceof RetrofitError)
                                return Observable.empty();
                            return Observable.error(throwable);
                        })
                        .subscribe(Void -> {}, error -> log.error(error.toString())));

                eventManager.getDeviceUpdated()
                    .addListener((DeviceUpdatedEvent event) -> deviceManager.get(event.getId())
                        .flatMap(device -> canSync()
                            .filter(canSync -> canSync)
                            .flatMap(Void -> deviceManager.performSync(device)))
                        .onErrorResumeNext(throwable -> {
                            if (throwable instanceof RetrofitError)
                                return Observable.empty();
                            return Observable.error(throwable);
                        })
                        .subscribe(Void -> {}, error -> log.error(error.toString())));

                Observable.interval(CRON_INTERVAL_IN_SECONDS, TimeUnit.SECONDS)
                    .flatMap(Void -> deviceSyncCron())
                    .onErrorResumeNext(throwable -> {
                        if (throwable instanceof RetrofitError)
                            return Observable.empty();
                        return Observable.error(throwable);
                    })
                    .subscribe(Void -> {}, error -> log.error(error.toString()));

                return Observable.empty();
            });
    }

    @Override
    public long order() {
        return 1512237076;
    }

    private Observable<Void> taskSyncCron() {
        TasksRequest tasksRequest = new TasksRequest();
        tasksRequest.setOnlyShowNeedSync(true);
        return canSync()
            .filter(canSync -> canSync)
            .flatMap(Void -> taskManager.get(tasksRequest))
            .map(TasksResponse::getTasks)
            .flatMap(Observable::from)
            .flatMap(taskManager::performSync)
            .ignoreElements().cast(Void.class);
    }

    private Observable<Void> inputSyncCron() {
        InputsRequest inputsRequest = new InputsRequest();
        inputsRequest.setOnlyShowNeedSync(true);
        return canSync()
            .filter(canSync -> canSync)
            .flatMap(Void -> inputManager.get(inputsRequest))
            .map(InputsResponse::getInputs)
            .flatMap(Observable::from)
            .flatMap(inputManager::performSync)
            .ignoreElements().cast(Void.class);
    }

    private Observable<Void> taskEntrySyncCron() {
        EntriesRequest entriesRequest = new EntriesRequest();
        entriesRequest.setOnlyShowNeedSync(true);
        return canSync()
            .filter(canSync -> canSync)
            .flatMap(Void -> taskEntryManager.get(entriesRequest))
            .map(EntriesResponse::getEntries)
            .flatMap(Observable::from)
            .flatMap(taskEntryManager::performSync)
            .ignoreElements().cast(Void.class);
    }

    private Observable<Void> deviceSyncCron() {
        DevicesRequest devicesRequest = new DevicesRequest();
        devicesRequest.setOnlyShowNeedSync(true);
        return canSync()
            .filter(canSync -> canSync)
            .flatMap(Void -> deviceManager.get(devicesRequest))
            .map(DevicesResponse::getDevices)
            .flatMap(Observable::from)
            .map(AggregateDevice::getDevice)
            .flatMap(deviceManager::performSync)
            .ignoreElements().cast(Void.class);
    }

    private Observable<Boolean> canSync() {
        return Observable.just(true);
    }
}
