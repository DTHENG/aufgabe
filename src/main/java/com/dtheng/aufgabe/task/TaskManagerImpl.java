package com.dtheng.aufgabe.task;

import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.input.dto.InputsRequest;
import com.dtheng.aufgabe.input.dto.InputsResponse;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.sync.SyncManager;
import com.dtheng.aufgabe.task.dto.*;
import com.dtheng.aufgabe.task.event.TaskCreatedEvent;
import com.dtheng.aufgabe.task.event.TaskUpdatedEvent;
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
    private InputManager inputManager;
    private TaskEntryManager taskEntryManager;
    private SyncManager syncManager;
    private ConfigManager configManager;
    private EventManager eventManager;

    @Inject
    public TaskManagerImpl(TaskDAO taskDAO, InputManager inputManager, TaskEntryManager taskEntryManager, SyncManager syncManager,
                           ConfigManager configManager, EventManager eventManager) {
        this.taskDAO = taskDAO;
        this.inputManager = inputManager;
        this.taskEntryManager = taskEntryManager;
        this.syncManager = syncManager;
        this.configManager = configManager;
        this.eventManager = eventManager;
    }

    @Override
    public Observable<AggregateTask> get(String id) {
        return taskDAO.getTask(id)
            .flatMap(this::aggregate);
    }

    @Override
    public Observable<AggregateTask> create(TaskCreateRequest request) {
        return configManager.getConfig()
            .map(AufgabeConfig::getDeviceType)
            .flatMap(deviceType -> {
                Task task = new Task();
                task.setId(request.getId().isPresent() ? request.getId().get() : "task-"+ new RandomString(8).nextString());
                task.setCreatedAt(request.getCreatedAt().isPresent() ? request.getCreatedAt().get() : new Date());
                task.setDescription(request.getDescription());
                return taskDAO.createTask(task);
            })
            .doOnNext(task -> eventManager.getTaskCreated().trigger(new TaskCreatedEvent(task.getId())))
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

    @Override
    public Observable<Task> performSync(Task task) {
        return syncManager.getSyncClient()
            .flatMap(syncClient -> syncClient.syncTask(
                new TaskSyncRequest(
                    task.getId(),
                    task.getCreatedAt().getTime(),
                    task.getDescription(),
                    task.getBonuslyMessage().orElse(null),
                    task.getSyncedAt().isPresent() ? task.getSyncedAt().get().getTime() : null))
                .defaultIfEmpty(null)
                .flatMap(Void -> taskDAO.setSyncedAt(task.getId(), new Date())));
    }

    @Override
    public Observable<AggregateTask> update(String id, TaskUpdateRequest request) {
        return taskDAO.update(id, request)
            .defaultIfEmpty(null)
            .flatMap(Void -> taskDAO.setUpdatedAt(id, new Date()))
            .doOnNext(task -> eventManager.getTaskUpdatedEvent().trigger(new TaskUpdatedEvent(task.getId())))
            .flatMap(this::aggregate);
    }

    private Observable<AggregateTask> aggregate(Task task) {
        InputsRequest inputsRequest = new InputsRequest();
        inputsRequest.setOffset(0);
        inputsRequest.setLimit(5);
        inputsRequest.setTaskId(Optional.of(task.getId()));
        return Observable.zip(
            inputManager.get(inputsRequest)
                .map(InputsResponse::getInputs),
            taskEntryManager.get(new EntriesRequest(0, 10, Optional.of(task.getId()), false))
                .map(EntriesResponse::getEntries),
            (inputs, entries) -> new AggregateTask(task, entries, inputs)) ;
    }
}