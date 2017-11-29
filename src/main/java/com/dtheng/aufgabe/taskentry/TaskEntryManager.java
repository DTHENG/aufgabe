package com.dtheng.aufgabe.taskentry;

import com.dtheng.aufgabe.taskentry.dto.EntriesRequest;
import com.dtheng.aufgabe.taskentry.dto.EntriesResponse;
import com.dtheng.aufgabe.taskentry.dto.TaskEntryCreateRequest;
import com.dtheng.aufgabe.taskentry.model.TaskEntry;
import com.google.inject.ImplementedBy;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(TaskEntryManagerImpl.class)
public interface TaskEntryManager {

	Observable<TaskEntry> get(String id);

	Observable<EntriesResponse> get(EntriesRequest request);

	Observable<TaskEntry> create(TaskEntryCreateRequest request);

    Observable<TaskEntry> performSyncRequest(TaskEntry task);
}
