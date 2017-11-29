package com.dtheng.aufgabe.task;

import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.DeviceType;
import com.dtheng.aufgabe.task.dto.AggregateTask;
import com.dtheng.aufgabe.task.dto.TasksRequest;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class TaskService {

    private TaskManager taskManager;
    private ConfigManager configManager;

    @Inject
    public TaskService(TaskManager taskManager, ConfigManager configManager) {
        this.taskManager = taskManager;
        this.configManager = configManager;
    }

    public Observable<Void> startUp() {
        Observable.interval(3, TimeUnit.MINUTES)
            .flatMap(Void -> taskSyncCron())
            .subscribe(Void -> {},
                error -> log.error(error.toString()));

        return Observable.empty();
    }

    private Observable<Void> taskSyncCron() {
        return Observable.defer(() -> configManager.getConfig()
            .filter(configuration -> configuration.getDeviceType() == DeviceType.RASPBERRY_PI)
            .flatMap(Void -> {
                TasksRequest tasksRequest = new TasksRequest();
                tasksRequest.setOnlyShowNeedSync(true);
                return taskManager.get(tasksRequest);
            })
            .flatMap(aggregateTasksResponse -> Observable.from(aggregateTasksResponse.getTasks())
            .map(AggregateTask::getTask)
            .flatMap(taskManager::performSyncRequest)
            .ignoreElements().cast(Void.class)));
    }
}
