package com.dtheng.aufgabe.security;

import com.dtheng.aufgabe.AufgabeModule;
import com.dtheng.aufgabe.config.ConfigManager;
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
public class SecurityManagerTest {

    private SecurityManager securityManager;
    private ConfigManager configManager;

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new AufgabeModule());
        securityManager = injector.getInstance(SecurityManager.class);
        configManager = injector.getInstance(ConfigManager.class);

        configManager.load(Optional.empty()).toBlocking().single();
    }

    @Test
    public void testSignatureCalc() throws Exception {
        String privateKey = configManager.getConfig().map(AufgabeConfig::getPrivateKey).toBlocking().single();
        String signature = securityManager.getSignature("{}").toBlocking().single();
        log.info("signature: \"{}\"", signature);
        Assert.assertEquals(signature, "16cf2d6de750394e1a6fe95eae60783301b8285ffc51da31da9be96111a6d0fc");
    }
}
