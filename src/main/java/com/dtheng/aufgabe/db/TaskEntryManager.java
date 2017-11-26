package com.dtheng.aufgabe.db;

import com.dtheng.aufgabe.db.dto.EntriesRequest;
import com.dtheng.aufgabe.db.dto.EntriesResponse;
import com.dtheng.aufgabe.db.dto.TaskEntryCreateRequest;
import com.dtheng.aufgabe.db.model.TaskEntry;
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
}
