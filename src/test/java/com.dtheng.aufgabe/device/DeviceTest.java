package com.dtheng.aufgabe.device;

import com.dtheng.aufgabe.AufgabeModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class DeviceTest {

    private DeviceService deviceService;

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new AufgabeModule());
        deviceService = injector.getInstance(DeviceService.class);
    }

    @Test
    public void testStartUp() throws Exception {
        deviceService.startUp().toBlocking().single();
    }

    @Test
    public void testGetDeviceId() throws Exception {
        deviceService.startUp().toBlocking().single();
        String deviceId = deviceService.getDeviceId().toBlocking().single();
        log.debug("deviceId: \"{}\"", deviceId);
        Assert.assertTrue(deviceId.length() > 0);
    }
}
