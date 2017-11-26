package com.dtheng.aufgabe.db;

import com.dtheng.aufgabe.db.dao.AufgabeDAO;
import com.dtheng.aufgabe.db.dto.EntriesRequest;
import com.dtheng.aufgabe.db.dto.EntriesResponse;
import com.dtheng.aufgabe.db.dto.TaskEntryCreateRequest;
import com.dtheng.aufgabe.db.model.TaskEntry;
import com.dtheng.aufgabe.db.util.RandomString;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.Date;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class TaskEntryManagerImpl implements TaskEntryManager {

	private AufgabeDAO aufgabeDAO;

	@Inject
	public TaskEntryManagerImpl(AufgabeDAO aufgabeDAO) {
		this.aufgabeDAO = aufgabeDAO;
	}

	@Override
	public Observable<TaskEntry> get(String id) {
		return aufgabeDAO.getTaskEntry(id);
	}

	@Override
	public Observable<TaskEntry> create(TaskEntryCreateRequest request) {
		TaskEntry entry = new TaskEntry();
		entry.setId(new RandomString(8).nextString());
		entry.setCreatedAt(new Date());
		entry.setDescription(request.getDescription());
		return aufgabeDAO.createTaskEntry(entry);
	}

	@Override
	public Observable<EntriesResponse> get(EntriesRequest request) {
		return aufgabeDAO.getEntries(request);
	}
}
