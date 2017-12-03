package com.dtheng.aufgabe.io;

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
public class FileManagerTest {

    private FileManager fileManager;

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new AufgabeModule());
        fileManager = injector.getInstance(FileManager.class);
    }

    @Test
    public void testRead() throws Exception {
        String file = fileManager.read("loghead.txt").toBlocking().single();
        log.debug(file);
        Assert.assertTrue(file.length() > 0);
    }
}
