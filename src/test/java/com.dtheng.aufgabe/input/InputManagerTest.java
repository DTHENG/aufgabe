package com.dtheng.aufgabe.input;

import com.dtheng.aufgabe.AufgabeModule;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.device.DeviceService;
import com.dtheng.aufgabe.input.dto.InputCreateRequest;
import com.dtheng.aufgabe.input.dto.InputsRequest;
import com.dtheng.aufgabe.input.dto.InputsResponse;
import com.dtheng.aufgabe.input.exception.InputNotFoundException;
import com.dtheng.aufgabe.input.handlers.B3F_TactileSwitchInputHandler;
import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.jooq.JooqService;
import com.dtheng.aufgabe.task.TaskManager;
import com.dtheng.aufgabe.task.dto.TaskCreateRequest;
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
public class InputManagerTest {

    private InputManager inputManager;
    private TaskManager taskManager;
    private ConfigManager configManager;
    private JooqService jooqService;
    private DeviceService deviceService;

    private Task testTask;

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new AufgabeModule());
        inputManager = injector.getInstance(InputManager.class);
        taskManager = injector.getInstance(TaskManager.class);
        configManager = injector.getInstance(ConfigManager.class);
        jooqService = injector.getInstance(JooqService.class);
        deviceService = injector.getInstance(DeviceService.class);

        configManager.load(Optional.empty()).toBlocking().single();
        jooqService.startUp().toBlocking().single();
        deviceService.startUp().toBlocking().single();

        TaskCreateRequest createRequest = new TaskCreateRequest();
        createRequest.setDescription("test task");
        testTask = taskManager.create(createRequest).toBlocking().single();
    }

    @Test
    public void testGet() throws Exception {
        InputCreateRequest createRequest = new InputCreateRequest();
        createRequest.setHandler(B3F_TactileSwitchInputHandler.class.getCanonicalName());
        createRequest.setIoPin("notAnIoPin");
        createRequest.setTaskId(testTask.getId());
        Input input = inputManager.create(createRequest).toBlocking().single();
        Input gotInput = inputManager.get(input.getId()).toBlocking().single();
        log.debug("input: {}", gotInput);
        Assert.assertEquals(input.getId(), gotInput.getId());
        Assert.assertEquals(input.getTaskId(), gotInput.getTaskId());
        Assert.assertEquals(input.getHandler(), gotInput.getHandler());
        Assert.assertEquals(input.getIoPin(), gotInput.getIoPin());
    }

    @Test
    public void testLookup() throws Exception {
        InputCreateRequest createRequest1 = new InputCreateRequest();
        createRequest1.setHandler(B3F_TactileSwitchInputHandler.class.getCanonicalName());
        createRequest1.setIoPin("notAnIoPin");
        createRequest1.setTaskId(testTask.getId());
        inputManager.create(createRequest1).toBlocking().single();
        InputCreateRequest createRequest2 = new InputCreateRequest();
        createRequest2.setHandler(B3F_TactileSwitchInputHandler.class.getCanonicalName());
        createRequest2.setIoPin("notAnIoPin");
        createRequest2.setTaskId(testTask.getId());
        inputManager.create(createRequest2).toBlocking().single();
        InputCreateRequest createRequest3 = new InputCreateRequest();
        createRequest3.setHandler(B3F_TactileSwitchInputHandler.class.getCanonicalName());
        createRequest3.setIoPin("notAnIoPin");
        createRequest3.setTaskId(testTask.getId());
        inputManager.create(createRequest3).toBlocking().single();
        InputsResponse response = inputManager.get(new InputsRequest()).toBlocking().single();
        Assert.assertTrue(response.getInputs().size() > 0);
    }

    @Test
    public void testCreate() throws Exception {
        InputCreateRequest createRequest = new InputCreateRequest();
        createRequest.setHandler(B3F_TactileSwitchInputHandler.class.getCanonicalName());
        createRequest.setIoPin("notAnIoPin");
        createRequest.setTaskId(testTask.getId());
        Input input = inputManager.create(createRequest).toBlocking().single();
        log.debug("input: {}", input);
        Assert.assertEquals(input.getIoPin(), "notAnIoPin");
        Assert.assertEquals(input.getTaskId(), testTask.getId());
        Assert.assertEquals(input.getHandler(), B3F_TactileSwitchInputHandler.class);
    }

    @Test(expected = InputNotFoundException.class)
    public void testRemove() throws Exception {
        InputCreateRequest createRequest = new InputCreateRequest();
        createRequest.setHandler(B3F_TactileSwitchInputHandler.class.getCanonicalName());
        createRequest.setIoPin("notAnIoPin");
        createRequest.setTaskId(testTask.getId());
        Input input = inputManager.create(createRequest).toBlocking().single();
        log.debug("input: {}", input);
        inputManager.remove(input.getId()).toBlocking().single();
        inputManager.get(input.getId()).toBlocking().single();
    }
}
