package com.dtheng.aufgabe.task;

import com.dtheng.aufgabe.AufgabeModule;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.device.DeviceService;
import com.dtheng.aufgabe.jooq.JooqService;
import com.dtheng.aufgabe.task.dto.*;
import com.dtheng.aufgabe.task.model.Task;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class TaskManagerTest {

    private TaskManager taskManager;
    private ConfigManager configManager;
    private JooqService jooqService;
    private DeviceService deviceService;

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new AufgabeModule());
        taskManager = injector.getInstance(TaskManager.class);
        configManager = injector.getInstance(ConfigManager.class);
        jooqService = injector.getInstance(JooqService.class);
        deviceService = injector.getInstance(DeviceService.class);

        configManager.load(Optional.empty()).toBlocking().single();
        jooqService.startUp().toBlocking().single();
        deviceService.startUp().toBlocking().single();
    }

    @Test
    public void testCreate() throws Exception {
        TaskCreateRequest createRequest = new TaskCreateRequest();
        createRequest.setDescription("test task");
        Task task = taskManager.create(createRequest).toBlocking().single();
        log.debug("task: {}", task);
        Assert.assertEquals(task.getDescription(), "test task");
    }

    @Test
    public void testGet() throws Exception {
        TaskCreateRequest createRequest = new TaskCreateRequest();
        createRequest.setDescription("test task");
        Task task = taskManager.create(createRequest).toBlocking().single();
        Task gotTask = taskManager.get(task.getId()).toBlocking().single();
        log.debug("task: {}", gotTask);
        Assert.assertEquals(task.getId(), gotTask.getId());
        Assert.assertEquals(task.getDescription(), gotTask.getDescription());
    }

    @Test
    public void testLookup() throws Exception {
        TaskCreateRequest createRequest1 = new TaskCreateRequest();
        createRequest1.setDescription("test task #1");
        taskManager.create(createRequest1).toBlocking().single();
        TaskCreateRequest createRequest2 = new TaskCreateRequest();
        createRequest2.setDescription("test task #2");
        taskManager.create(createRequest2).toBlocking().single();
        TaskCreateRequest createRequest3 = new TaskCreateRequest();
        createRequest3.setDescription("test task #3");
        taskManager.create(createRequest3).toBlocking().single();
        TasksResponse response = taskManager.get(new TasksRequest()).toBlocking().single();
        log.debug("response: {}", response);
        Assert.assertTrue(response.getTasks().size() > 0);
    }

    @Test
    public void testUpdate() throws Exception {
        TaskCreateRequest createRequest = new TaskCreateRequest();
        createRequest.setDescription("test task");
        Task task = taskManager.create(createRequest).toBlocking().single();
        TaskUpdateRequest updateRequest = new TaskUpdateRequest();
        updateRequest.setDescription(Optional.of("test task edited"));
        Task updatedTask = taskManager.update(task.getId(), updateRequest).toBlocking().single();
        log.debug("task: {}", updatedTask);
        Assert.assertEquals(updateRequest.getDescription().get(), updatedTask.getDescription());
    }
}
