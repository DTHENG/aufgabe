package com.dtheng.aufgabe;

import com.dtheng.aufgabe.http.AufgabeApi;
import com.dtheng.aufgabe.http.ServletManager;
import com.google.common.collect.ImmutableMap;
import com.google.inject.*;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.HashMap;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class Aufgabe {

    public static void main(String args[]) {

        Injector injector = Guice.createInjector(new AufgabeModule());
        AufgabeContext context = injector.getInstance(AufgabeContext.class);
        context.setInjector(injector);

        StartUp startUp = context.getInjector().getInstance(StartUp.class);

        startUp.start()
                .subscribe(Void -> {},
                        error -> log.error(error.toString()));
    }

    private static class StartUp {

        private ServletManager servletManager;

        @Inject
        public StartUp(ServletManager servletManager) {
            this.servletManager = servletManager;
        }

        public Observable<Void> start() {

            // Start the http server
            return servletManager.start(new HashMap<>(ImmutableMap.of(
                    "/", AufgabeApi.NotFound.class,
                    "/entries", AufgabeApi.Entries.class
            )));
        }
    }
}
