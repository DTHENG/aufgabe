package com.dtheng.aufgabe.http;

import com.dtheng.aufgabe.AufgabeContext;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import rx.Observable;

import javax.servlet.Servlet;
import java.util.Map;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class ServletManagerImpl implements ServletManager {

    private AufgabeContext context;

    @Inject
    public ServletManagerImpl(AufgabeContext context) {
        this.context = context;
    }

    @Override
    public Observable<Void> start(Integer port, Map<String, Class<? extends Servlet>> config) {
        log.info("Starting HTTP server...");
        return Observable.defer(() -> {
            Server server = new Server(port);
            Context jettyContext = new Context();
            return Observable.from(config.keySet()).zipWith(Observable.from(config.values()),
                (path, servletClass) -> {
                    Servlet injectedClass = context.getInjector().getInstance(servletClass);
                    jettyContext.addServlet(new ServletHolder(injectedClass), path);
                    return Observable.empty();
                }).flatMap(o -> o)
                .defaultIfEmpty(null)
                .toList()
                .flatMap(Void -> {
                    server.setHandler(jettyContext);
                    try {
                        server.start();
                        log.info("Server running on port {}!", port);
                        server.join();
                        return Observable.empty();
                    } catch (Throwable throwable) {
                        return Observable.error(throwable);
                    }
                });
        }).ignoreElements().cast(Void.class);
    }
}
