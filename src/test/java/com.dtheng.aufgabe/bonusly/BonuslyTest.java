package com.dtheng.aufgabe.bonusly;

import com.dtheng.aufgabe.AufgabeModule;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import retrofit.RetrofitError;

import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class BonuslyTest {

    private ConfigManager configManager;
    private BonuslyService bonuslyService;

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new AufgabeModule());
        configManager = injector.getInstance(ConfigManager.class);
        bonuslyService = injector.getInstance(BonuslyService.class);
        configManager.load(Optional.of("configuration-test.json")).toBlocking().single();
        bonuslyService.startUp().toBlocking().single();
    }

    @Test
    public void testConfig() throws Exception {
        AufgabeConfig config = configManager.getConfig().toBlocking().single();
        Assert.assertTrue(config.getBonuslyAccessToken().isPresent());
        Assert.assertTrue(config.getBonuslyApiEndpoint().isPresent());
        Assert.assertTrue(config.isBonuslyEnabled());
    }

    @Test(expected = RetrofitError.class)
    public void testSubmitRequest() throws Exception {
        bonuslyService.test().toBlocking().subscribe();
    }
}