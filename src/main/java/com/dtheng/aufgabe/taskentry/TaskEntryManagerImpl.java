package com.dtheng.aufgabe.taskentry;

import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.Configuration;
import com.dtheng.aufgabe.config.model.DeviceType;
import com.dtheng.aufgabe.exceptions.UnsupportedException;
import com.dtheng.aufgabe.taskentry.dto.EntriesRequest;
import com.dtheng.aufgabe.taskentry.dto.EntriesResponse;
import com.dtheng.aufgabe.taskentry.dto.TaskEntryCreateRequest;
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

    @Inject
    public TaskEntryManagerImpl(TaskEntryDAO taskEntryDAO, ConfigManager configManager) {
        this.taskEntryDAO = taskEntryDAO;
        this.configManager = configManager;
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
                if (deviceType != DeviceType.RASPBERRY_PI)
                    return Observable.error(new UnsupportedException());
                TaskEntry entry = new TaskEntry();
                entry.setId("entry-"+ new RandomString(8).nextString());
                entry.setCreatedAt(new Date());
                entry.setTaskId(request.getTaskId());
                return taskEntryDAO.createTaskEntry(entry);
            });
    }

    @Override
    public Observable<EntriesResponse> get(EntriesRequest request) {
        return taskEntryDAO.getEntries(request);
    }
}
