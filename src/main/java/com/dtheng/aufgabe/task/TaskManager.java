package com.dtheng.aufgabe.task;

import com.dtheng.aufgabe.task.dto.*;
import com.dtheng.aufgabe.task.model.Task;
import com.google.inject.ImplementedBy;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(TaskManagerImpl.class)
public interface TaskManager {

	Observable<AggregateTask> get(String id);

	Observable<AggregateTasksResponse> get(TasksRequest request);

	Observable<AggregateTask> create(TaskCreateRequest request);

    Observable<Task> performSync(Task task);

    Observable<AggregateTask> update(String id, TaskUpdateRequest request);
}
