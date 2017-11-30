package com.dtheng.aufgabe.taskentry;

import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.Configuration;
import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.sync.SyncManager;
import com.dtheng.aufgabe.taskentry.dto.EntriesRequest;
import com.dtheng.aufgabe.taskentry.dto.EntriesResponse;
import com.dtheng.aufgabe.taskentry.dto.TaskEntryCreateRequest;
import com.dtheng.aufgabe.taskentry.dto.TaskEntrySyncRequest;
import com.dtheng.aufgabe.taskentry.event.TaskEntryCreatedEvent;
import com.dtheng.aufgabe.taskentry.model.TaskEntry;
import com.dtheng.aufgabe.util.RandomString;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.Date;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class TaskEntryManagerImpl implements TaskEntryManager {

    private TaskEntryDAO taskEntryDAO;
    private ConfigManager configManager;
    private SyncManager syncManager;
    private EventManager eventManager;

    @Inject
    public TaskEntryManagerImpl(TaskEntryDAO taskEntryDAO, ConfigManager configManager, SyncManager syncManager, EventManager eventManager) {
        this.taskEntryDAO = taskEntryDAO;
        this.configManager = configManager;
        this.syncManager = syncManager;
        this.eventManager = eventManager;
    }

    @Override
    public Observable<TaskEntry> get(String id) {
        return taskEntryDAO.getTaskEntry(id);
    }

    @Override
    public Observable<TaskEntry> create(TaskEntryCreateRequest request) {
        return configManager.getConfig()
            .map(Configuration::getDeviceType)
            .flatMap(deviceType -> {
                TaskEntry entry = new TaskEntry();
                if (request.getId().isPresent())
                    entry.setId(request.getId().get());
                else
                    entry.setId("entry-"+ new RandomString(8).nextString());
                if (request.getCreatedAt().isPresent())
                    entry.setCreatedAt(request.getCreatedAt().get());
                else
                    entry.setCreatedAt(new Date());
                entry.setTaskId(request.getTaskId());
                entry.setInputId(request.getInputId());
                return taskEntryDAO.createTaskEntry(entry)
                    .doOnNext(taskEntry -> eventManager.getTaskEntryCreated().trigger(new TaskEntryCreatedEvent(taskEntry.getId())));
            });
    }

    @Override
    public Observable<EntriesResponse> get(EntriesRequest request) {
        return taskEntryDAO.getEntries(request);
    }

    @Override
    public Observable<TaskEntry> performSync(TaskEntry taskEntry) {
        return syncManager.getSyncClient()
            .flatMap(syncClient -> syncClient.syncTaskEntry(new TaskEntrySyncRequest(taskEntry.getId(), taskEntry.getCreatedAt().getTime(), taskEntry.getTaskId(), taskEntry.getInputId()))
                .defaultIfEmpty(null)
                .flatMap(Void -> taskEntryDAO.setSyncedAt(taskEntry.getId(), new Date())));
    }
}
