package com.dtheng.aufgabe.config;

import com.dtheng.aufgabe.AufgabeModule;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
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
public class ConfigTest {

    private ConfigManager configManager;

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new AufgabeModule());
        configManager = injector.getInstance(ConfigManager.class);
    }

    @Test
    public void testLoad() throws Exception {
        configManager.load(Optional.empty()).toBlocking().single();
    }

    @Test
    public void testDefaultConfig() throws Exception {
        configManager.load(Optional.empty()).toBlocking().single();
        AufgabeConfig config = configManager.getConfig().toBlocking().single();
        Assert.assertTrue(config.getHttpPort() == 8080);
        Assert.assertFalse(config.getSyncRemoteIp().isPresent());
        Assert.assertFalse(config.getBonuslyApiEndpoint().isPresent());
        Assert.assertFalse(config.getBonuslyAccessToken().isPresent());
        Assert.assertFalse(config.isBonuslyEnabled());
        Assert.assertTrue(config.getDatabaseName().equals("aufgabe"));
        Assert.assertTrue(config.getDatabasePassword().equals("abcd1234"));
        Assert.assertTrue(config.getDatabaseUser().equals("root"));
    }

    @Test(expected = RuntimeException.class)
    public void testLoadWithInvalidConfigFilename() throws Exception {
        configManager.load(Optional.of("configuration-notafile.json")).toBlocking().single();
    }
}
