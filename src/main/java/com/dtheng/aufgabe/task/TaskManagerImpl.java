package com.dtheng.aufgabe.task;

import com.dtheng.aufgabe.button.ButtonManager;
import com.dtheng.aufgabe.button.dto.ButtonsRequest;
import com.dtheng.aufgabe.button.dto.ButtonsResponse;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.Configuration;
import com.dtheng.aufgabe.config.model.DeviceType;
import com.dtheng.aufgabe.exceptions.UnsupportedException;
import com.dtheng.aufgabe.sync.SyncManager;
import com.dtheng.aufgabe.task.dto.*;
import com.dtheng.aufgabe.task.model.Task;
import com.dtheng.aufgabe.taskentry.TaskEntryManager;
import com.dtheng.aufgabe.taskentry.dto.EntriesRequest;
import com.dtheng.aufgabe.taskentry.dto.EntriesResponse;
import com.dtheng.aufgabe.util.RandomString;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.Date;
import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class TaskManagerImpl implements TaskManager {

    private TaskDAO taskDAO;
    private ButtonManager buttonManager;
    private TaskEntryManager taskEntryManager;
    private SyncManager syncManager;
    private ConfigManager configManager;

    @Inject
    public TaskManagerImpl(TaskDAO taskDAO, ButtonManager buttonManager, TaskEntryManager taskEntryManager, SyncManager syncManager, ConfigManager configManager) {
        this.taskDAO = taskDAO;
        this.buttonManager = buttonManager;
        this.taskEntryManager = taskEntryManager;
        this.syncManager = syncManager;
        this.configManager = configManager;
    }

    @Override
    public Observable<AggregateTask> get(String id) {
        return taskDAO.getTask(id)
            .flatMap(this::aggregate);
    }

    @Override
    public Observable<AggregateTask> create(TaskCreateRequest request) {
        return configManager.getConfig()
            .map(Configuration::getDeviceType)
            .flatMap(deviceType -> {
                if (deviceType != DeviceType.RASPBERRY_PI)
                    return Observable.error(new UnsupportedException());
                Task task = new Task();
                task.setId("task-"+ new RandomString(8).nextString());
                task.setCreatedAt(new Date());
                task.setDescription(request.getDescription());
                return Observable.zip(
                    taskDAO.createTask(task),
                    syncManager.getSyncClient(),
                    (newTask, syncClient) ->
                        Observable.defer(() ->
                            Observable.just(syncClient.syncTask(new TaskSyncRequest(newTask.getId(), newTask.getCreatedAt().getTime(), newTask.getDescription())).toBlocking().single()))
                            .defaultIfEmpty(null)
                            .map(Void -> newTask));
            })
            .flatMap(o -> o)
            .flatMap(this::aggregate);
    }

    @Override
    public Observable<AggregateTasksResponse> get(TasksRequest request) {
        return taskDAO.getTasks(request)
            .flatMap(tasksResponse -> Observable.from(tasksResponse.getTasks())
                .concatMap(this::aggregate)
                .toList()
                .map(aggregateTasks -> new AggregateTasksResponse(tasksResponse.getOffset(), tasksResponse.getLimit(), tasksResponse.getTotal(), aggregateTasks)));
    }

    private Observable<AggregateTask> aggregate(Task task) {
        ButtonsRequest buttonsRequest = new ButtonsRequest();
        buttonsRequest.setOffset(0);
        buttonsRequest.setLimit(10);
        buttonsRequest.setTaskId(Optional.of(task.getId()));
        return Observable.zip(
            buttonManager.get(buttonsRequest)
                .map(ButtonsResponse::getButtons),
            taskEntryManager.get(new EntriesRequest(0, 10, Optional.of(task.getId())))
                .map(EntriesResponse::getEntries),
            (buttons, entries) -> new AggregateTask(task, entries, buttons)) ;
    }
}