package com.dtheng.aufgabe;

import com.dtheng.aufgabe.http.AufgabeApi;
import com.dtheng.aufgabe.http.ServletManager;
import com.google.common.collect.ImmutableMap;
import com.google.inject.*;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.HashMap;
import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class Aufgabe {

    public static void main(String args[]) {

        if (args.length != 1)
            throw new RuntimeException("Expecting port argument, ie: java -jar aufgabe.jar 8080");

        Optional<Integer> port = Optional.ofNullable(Integer.valueOf(args[0]));
        if ( ! port.isPresent())
            throw new RuntimeException("Invalid port argument: "+ args[0]);

        Injector injector = Guice.createInjector(new AufgabeModule());
        AufgabeContext context = injector.getInstance(AufgabeContext.class);
        context.setInjector(injector);

        StartUp startUp = context.getInjector().getInstance(StartUp.class);

        startUp.start(port.get())
                .subscribe(Void -> {},
                        error -> log.error(error.toString()));
    }

    private static class StartUp {

        private ServletManager servletManager;

        @Inject
        public StartUp(ServletManager servletManager) {
            this.servletManager = servletManager;
        }

        public Observable<Void> start(Integer port) {

            // Start the http server
            return servletManager.start(port, new HashMap<>(ImmutableMap.of(
                    "/", AufgabeApi.NotFound.class,
                    "/entries", AufgabeApi.Entries.class
            )));
        }
    }
}
