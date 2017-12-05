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

	Observable<Task> get(String id);

	Observable<TasksResponse> get(TasksRequest request);

	Observable<Task> create(TaskCreateRequest request);

    Observable<Task> performSync(Task task);

    Observable<Task> update(String id, TaskUpdateRequest request);
}
