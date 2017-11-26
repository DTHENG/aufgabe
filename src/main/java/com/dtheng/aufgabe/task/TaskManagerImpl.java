package com.dtheng.aufgabe.task;

import com.dtheng.aufgabe.button.ButtonManager;
import com.dtheng.aufgabe.button.dto.ButtonsRequest;
import com.dtheng.aufgabe.button.dto.ButtonsResponse;
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

    @Inject
    public TaskManagerImpl(TaskDAO taskDAO, ButtonManager buttonManager, TaskEntryManager taskEntryManager) {
        this.taskDAO = taskDAO;
        this.buttonManager = buttonManager;
        this.taskEntryManager = taskEntryManager;
    }

    @Override
    public Observable<AggregateTask> get(String id) {
        return taskDAO.getTask(id)
                .flatMap(this::aggregate);
    }

    @Override
    public Observable<AggregateTask> create(TaskCreateRequest request) {
        Task task = new Task();
        task.setId(new RandomString(8).nextString());
        task.setCreatedAt(new Date());
        task.setDescription(request.getDescription());
        return taskDAO.createTask(task)
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
        return Observable.zip(
                buttonManager.get(new ButtonsRequest(0, 10, Optional.of(task.getId())))
                    .map(ButtonsResponse::getButtons),
                taskEntryManager.get(new EntriesRequest(0, 10, Optional.of(task.getId())))
                    .map(EntriesResponse::getEntries),
                (buttons, entries) -> new AggregateTask(task, entries, buttons));
    }
}
