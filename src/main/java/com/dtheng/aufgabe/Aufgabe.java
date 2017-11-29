package com.dtheng.aufgabe;

import com.dtheng.aufgabe.input.InputApi;
import com.dtheng.aufgabe.config.ConfigApi;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.device.DeviceManager;
import com.dtheng.aufgabe.jooq.JooqManager;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.ServletManager;
import com.dtheng.aufgabe.io.RaspberryPiManager;
import com.dtheng.aufgabe.stats.StatsApi;
import com.dtheng.aufgabe.sync.SyncApi;
import com.dtheng.aufgabe.sync.SyncManager;
import com.dtheng.aufgabe.task.TaskApi;
import com.dtheng.aufgabe.taskentry.EntryApi;
import com.dtheng.aufgabe.taskentry.TaskEntryService;
import com.google.inject.*;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import javax.servlet.Servlet;
import java.util.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class Aufgabe {

    public static void main(String args[]) {

        Optional<String> customConfigFileName = Optional.ofNullable(args.length > 0 ? args[0] : null);

        Injector injector = Guice.createInjector(new AufgabeModule());
        AufgabeContext context = injector.getInstance(AufgabeContext.class);
        context.setInjector(injector);

        StartUp startUp = context.getInjector().getInstance(StartUp.class);

        startUp.start(customConfigFileName)
            .subscribe(Void -> {},
                error -> {
                    error.printStackTrace();
                    log.error(error.toString());
                });
    }

    private static class StartUp {

        private ServletManager servletManager;
        private ConfigManager configManager;
        private JooqManager jooqManager;
        private RaspberryPiManager raspberryPiManager;
        private TaskEntryService taskEntryService;
        private DeviceManager deviceManager;
        private SyncManager syncManager;

        @Inject
        public StartUp(ServletManager servletManager, ConfigManager configManager, JooqManager jooqManager, RaspberryPiManager raspberryPiManager, TaskEntryService taskEntryService, DeviceManager deviceManager, SyncManager syncManager) {
            this.servletManager = servletManager;
            this.configManager = configManager;
            this.jooqManager = jooqManager;
            this.raspberryPiManager = raspberryPiManager;
            this.taskEntryService = taskEntryService;
            this.deviceManager = deviceManager;
            this.syncManager = syncManager;
        }

        Observable<Void> start(Optional<String> customConfigFileName) {
            return configManager.load(customConfigFileName)
                .defaultIfEmpty(null)
                .flatMap(Void -> configManager.getConfig())
                .flatMap(config -> {

                    Map<String, Class<? extends Servlet>> routes = new HashMap<>();

                    routes.put("/", AufgabeServlet.class);
                    routes.put("/entries", EntryApi.Entries.class);
                    routes.put("/entry/*", EntryApi.GetEntry.class);
                    routes.put("/task", TaskApi.CreateTask.class);
                    routes.put("/tasks", TaskApi.Tasks.class);
                    routes.put("/taskFromId/*", TaskApi.GetTask.class);
                    routes.put("/input", InputApi.CreateCreate.class);
                    routes.put("/inputs", InputApi.Inputs.class);
                    routes.put("/inputFromId/*", InputApi.GetInput.class);
                    routes.put("/removeInput/*", InputApi.RemoveInput.class);

                    routes.put("/config", ConfigApi.InputConfig.class);
                    routes.put("/stats", StatsApi.Default.class);

                    routes.put("/sync/task", SyncApi.SyncTask.class);

                    return Observable.concat(Arrays.asList(

                        deviceManager.startUp(),

                        // Create database connection
                        jooqManager.startUp(),

                        // Configure IO pins on raspberry pi
                        raspberryPiManager.startUp(),

                        taskEntryService.startUp(),

                        syncManager.startUp(),

                        // Start the http server
                        servletManager.start(config.getHttpPort(), routes))
                    );
                });
        }
    }
}