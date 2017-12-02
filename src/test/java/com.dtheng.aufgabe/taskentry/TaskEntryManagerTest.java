package com.dtheng.aufgabe.taskentry;

import com.dtheng.aufgabe.AufgabeModule;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.device.DeviceService;
import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.input.dto.InputCreateRequest;
import com.dtheng.aufgabe.input.handlers.B3F_TactileSwitchInputHandler;
import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.jooq.JooqService;
import com.dtheng.aufgabe.task.TaskManager;
import com.dtheng.aufgabe.task.dto.AggregateTask;
import com.dtheng.aufgabe.task.dto.TaskCreateRequest;
import com.dtheng.aufgabe.task.model.Task;
import com.dtheng.aufgabe.taskentry.dto.EntriesRequest;
import com.dtheng.aufgabe.taskentry.dto.EntriesResponse;
import com.dtheng.aufgabe.taskentry.dto.TaskEntryCreateRequest;
import com.dtheng.aufgabe.taskentry.model.TaskEntry;
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
public class TaskEntryManagerTest {

    private TaskManager taskManager;
    private TaskEntryManager taskEntryManager;
    private InputManager inputManager;
    private ConfigManager configManager;
    private JooqService jooqService;
    private DeviceService deviceService;

    private Task testTask;
    private Input testInput;

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new AufgabeModule());
        taskManager = injector.getInstance(TaskManager.class);
        taskEntryManager = injector.getInstance(TaskEntryManager.class);
        inputManager = injector.getInstance(InputManager.class);
        configManager = injector.getInstance(ConfigManager.class);
        jooqService = injector.getInstance(JooqService.class);
        deviceService = injector.getInstance(DeviceService.class);

        configManager.load(Optional.empty()).toBlocking().single();
        jooqService.startUp().toBlocking().single();
        deviceService.startUp().toBlocking().single();

        TaskCreateRequest createRequest = new TaskCreateRequest();
        createRequest.setDescription("test task");
        testTask = taskManager.create(createRequest).map(AggregateTask::getTask).toBlocking().single();

        InputCreateRequest inputCreateRequest = new InputCreateRequest();
        inputCreateRequest.setHandler(B3F_TactileSwitchInputHandler.class.getCanonicalName());
        inputCreateRequest.setIoPin("notAnIoPin");
        inputCreateRequest.setTaskId(testTask.getId());
        testInput = inputManager.create(inputCreateRequest).toBlocking().single();

    }

    @Test
    public void testCreate() throws Exception {
        TaskEntryCreateRequest createRequest = new TaskEntryCreateRequest();
        createRequest.setTaskId(testTask.getId());
        createRequest.setInputId(testInput.getId());
        TaskEntry taskEntry = taskEntryManager.create(createRequest).toBlocking().single();
        log.debug("task entry: {}", taskEntry);
        Assert.assertEquals(taskEntry.getTaskId(), testTask.getId());
        Assert.assertEquals(taskEntry.getInputId(), testInput.getId());
    }

    @Test
    public void testGet() throws Exception {
        TaskEntryCreateRequest createRequest = new TaskEntryCreateRequest();
        createRequest.setTaskId(testTask.getId());
        createRequest.setInputId(testInput.getId());
        TaskEntry taskEntry = taskEntryManager.create(createRequest).toBlocking().single();
        TaskEntry gotTaskEntry = taskEntryManager.get(taskEntry.getId()).toBlocking().single();
        log.debug("task entry: {}", gotTaskEntry);
        Assert.assertEquals(taskEntry.getId(), gotTaskEntry.getId());
        Assert.assertEquals(taskEntry.getTaskId(), gotTaskEntry.getTaskId());
        Assert.assertEquals(taskEntry.getInputId(), gotTaskEntry.getInputId());
        Assert.assertEquals(taskEntry.getCreatedAt(), gotTaskEntry.getCreatedAt());
    }

    @Test
    public void testLookup() throws Exception {
        TaskEntryCreateRequest createRequest1 = new TaskEntryCreateRequest();
        createRequest1.setTaskId(testTask.getId());
        createRequest1.setInputId(testInput.getId());
        taskEntryManager.create(createRequest1).toBlocking().single();
        TaskEntryCreateRequest createRequest2 = new TaskEntryCreateRequest();
        createRequest2.setTaskId(testTask.getId());
        createRequest2.setInputId(testInput.getId());
        taskEntryManager.create(createRequest2).toBlocking().single();
        TaskEntryCreateRequest createRequest3 = new TaskEntryCreateRequest();
        createRequest3.setTaskId(testTask.getId());
        createRequest3.setInputId(testInput.getId());
        taskEntryManager.create(createRequest3).toBlocking().single();
        EntriesResponse response = taskEntryManager.get(new EntriesRequest()).toBlocking().single();
        log.debug("response: {}", response);
        Assert.assertTrue(response.getEntries().size() > 0);
    }
}
