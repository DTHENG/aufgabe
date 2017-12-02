package com.dtheng.aufgabe.jooq;

import com.dtheng.aufgabe.AufgabeModule;
import com.dtheng.aufgabe.config.ConfigManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class JooqTest {

    private ConfigManager configManager;
    private JooqService jooqService;

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new AufgabeModule());
        configManager = injector.getInstance(ConfigManager.class);
        configManager.load(Optional.empty()).toBlocking().single();
        jooqService = injector.getInstance(JooqService.class);
        jooqService.startUp().toBlocking().single();
    }

    @Test
    public void testGetConnection() throws Exception {
        jooqService.getConnection().toBlocking().single();
    }
}
