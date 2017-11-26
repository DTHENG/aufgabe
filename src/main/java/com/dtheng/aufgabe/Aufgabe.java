package com.dtheng.aufgabe;

import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.db.JooqManager;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.ServletManager;
import com.dtheng.aufgabe.io.RaspberryPiManager;
import com.google.common.collect.ImmutableMap;
import com.google.inject.*;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

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
                        error -> log.error(error.toString()));
    }

    private static class StartUp {

        private ServletManager servletManager;
		private ConfigManager configManager;
		private JooqManager jooqManager;
		private RaspberryPiManager raspberryPiManager;

        @Inject
        public StartUp(ServletManager servletManager, ConfigManager configManager, JooqManager jooqManager, RaspberryPiManager raspberryPiManager) {
            this.servletManager = servletManager;
            this.configManager = configManager;
            this.jooqManager = jooqManager;
            this.raspberryPiManager = raspberryPiManager;
        }

        public Observable<Void> start(Optional<String> customConfigFileName) {
			return configManager.load(customConfigFileName)
					.defaultIfEmpty(null)
					.flatMap(Void -> configManager.getConfig())
					.flatMap(config ->

						Observable.concat(Arrays.asList(

							// Create database connection
							jooqManager.startUp(),

							// Configure IO pins on raspberry pi
							raspberryPiManager.startUp(),

							// Start the http server
							servletManager.start(config.getHttpPort(), new HashMap<>(ImmutableMap.of(
									"/", AufgabeServlet.class,
									"/entries", AufgabeApi.Entries.class,
									"/entry/*", AufgabeApi.GetEntry.class
							))))
						));
        }
    }
}
