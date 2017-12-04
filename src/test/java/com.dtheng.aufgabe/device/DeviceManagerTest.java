package com.dtheng.aufgabe.device;

import com.dtheng.aufgabe.AufgabeModule;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.device.dto.DeviceCreateRequest;
import com.dtheng.aufgabe.device.dto.DevicesRequest;
import com.dtheng.aufgabe.device.dto.DevicesResponse;
import com.dtheng.aufgabe.device.model.Device;
import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.jooq.JooqService;
import com.dtheng.aufgabe.task.TaskManager;
import com.dtheng.aufgabe.task.dto.AggregateTask;
import com.dtheng.aufgabe.task.dto.TaskCreateRequest;
import com.dtheng.aufgabe.task.model.Task;
import com.dtheng.aufgabe.util.RandomString;
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
public class DeviceManagerTest {

    private InputManager inputManager;
    private TaskManager taskManager;
    private ConfigManager configManager;
    private JooqService jooqService;
    private DeviceService deviceService;
    private DeviceManager deviceManager;

    private Task testTask;

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new AufgabeModule());
        inputManager = injector.getInstance(InputManager.class);
        taskManager = injector.getInstance(TaskManager.class);
        configManager = injector.getInstance(ConfigManager.class);
        jooqService = injector.getInstance(JooqService.class);
        deviceService = injector.getInstance(DeviceService.class);
        deviceManager = injector.getInstance(DeviceManager.class);

        configManager.load(Optional.empty()).toBlocking().single();
        jooqService.startUp().toBlocking().single();
        deviceService.startUp().toBlocking().single();

        TaskCreateRequest createRequest = new TaskCreateRequest();
        createRequest.setDescription("test task");
        testTask = taskManager.create(createRequest).map(AggregateTask::getTask).toBlocking().single();
    }

    @Test
    public void testGet() throws Exception {
        DeviceCreateRequest createRequest = new DeviceCreateRequest();
        createRequest.setId(Optional.of(new RandomString(5).nextString()));
        Device device = deviceManager.create(createRequest).toBlocking().single();
        Device gotDevice = deviceManager.get(device.getId()).toBlocking().single();
        log.debug("device: {}", gotDevice);
        Assert.assertEquals(device.getId(), gotDevice.getId());
    }

    @Test
    public void testLookup() throws Exception {
        DeviceCreateRequest createRequest1 = new DeviceCreateRequest();
        createRequest1.setId(Optional.of(new RandomString(5).nextString()));
        deviceManager.create(createRequest1).toBlocking().single();
        DeviceCreateRequest createRequest2 = new DeviceCreateRequest();
        createRequest2.setId(Optional.of(new RandomString(5).nextString()));
        deviceManager.create(createRequest2).toBlocking().single();
        DeviceCreateRequest createRequest3 = new DeviceCreateRequest();
        createRequest3.setId(Optional.of(new RandomString(5).nextString()));
        deviceManager.create(createRequest3).toBlocking().single();
        DevicesResponse response = deviceManager.get(new DevicesRequest()).toBlocking().single();
        Assert.assertTrue(response.getDevices().size() > 0);
    }

    @Test
    public void testCreate() throws Exception {
        DeviceCreateRequest createRequest = new DeviceCreateRequest();
        createRequest.setName(Optional.of("name"));
        createRequest.setDescription(Optional.of("description"));
        createRequest.setId(Optional.of(new RandomString(5).nextString()));
        Device device = deviceManager.create(createRequest).toBlocking().single();
        log.debug("device: {}", device);
        Assert.assertEquals(device.getName().get(), "name");
        Assert.assertEquals(device.getDescription().get(), "description");
    }
}
