package com.dtheng.aufgabe;

import com.dtheng.aufgabe.input.InputApi;
import com.dtheng.aufgabe.config.ConfigApi;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.io.FileManager;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.ServletManager;
import com.dtheng.aufgabe.jooq.JooqManager;
import com.dtheng.aufgabe.stats.StatsApi;
import com.dtheng.aufgabe.sync.SyncApi;
import com.dtheng.aufgabe.sync.SyncService;
import com.dtheng.aufgabe.task.TaskApi;
import com.dtheng.aufgabe.taskentry.EntryApi;
import com.google.inject.*;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
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
        private AufgabeContext aufgabeContext;
        private FileManager fileManager;
        private JooqManager jooqManager;
        private SyncService syncService;

        @Inject
        public StartUp(ServletManager servletManager, ConfigManager configManager, AufgabeContext aufgabeContext, FileManager fileManager, JooqManager jooqManager, SyncService syncService) {
            this.servletManager = servletManager;
            this.configManager = configManager;
            this.aufgabeContext = aufgabeContext;
            this.fileManager = fileManager;
            this.jooqManager = jooqManager;
            this.syncService = syncService;
        }

        Observable<Void> start(Optional<String> customConfigFileName) {
            return fileManager.read("loghead.txt")
                .doOnNext(log::info)
                .flatMap(Void -> configManager.load(customConfigFileName))
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
                    routes.put("/task/update/*", TaskApi.UpdateTask.class);
                    routes.put("/input", InputApi.CreateCreate.class);
                    routes.put("/inputs", InputApi.Inputs.class);
                    routes.put("/inputFromId/*", InputApi.GetInput.class);
                    routes.put("/removeInput/*", InputApi.RemoveInput.class);

                    routes.put("/config", ConfigApi.InputConfig.class);
                    routes.put("/stats", StatsApi.Default.class);

                    routes.put("/sync/task", SyncApi.SyncTask.class);
                    routes.put("/sync/entry", SyncApi.SyncEntry.class);
                    routes.put("/sync/input", SyncApi.SyncInput.class);

                    return Observable.concat(Arrays.asList(

                        // 1. Connect database
                        jooqManager.start(),

                        // 2. Start services
                        startServices(),

                        syncService.start(),

                        // 3. Start http server
                        servletManager.start(config.getHttpPort(), routes)))

                        .defaultIfEmpty(null)
                        .toList();
                })
                .ignoreElements().cast(Void.class);
        }

        /**
         * Inject and start services
         *
         * @return nope
         */
        private Observable<Void> startServices() {
            Reflections reflections = new Reflections("com.dtheng.aufgabe");
            Set<Class<? extends AufgabeService>> classes = reflections.getSubTypesOf(AufgabeService.class);
            return Observable.from(classes)
                .map(aufgabeContext.getInjector()::getInstance)
                .flatMap(AufgabeService::startUp)
                .defaultIfEmpty(null)
                .toList()
                .ignoreElements().cast(Void.class);
        }
    }
}