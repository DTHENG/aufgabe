package com.dtheng.aufgabe.http;

import com.dtheng.aufgabe.AufgabeContext;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public Observable<Void> start(Integer port, Map<String, Class<? extends Servlet>> config, Map<String, Object> metaData) {
        Server server = new Server(port);
        Context jettyContext = new Context();
        return configureServlets(config, jettyContext)
            .defaultIfEmpty(null)
            .flatMap(Void -> {
                server.setHandler(jettyContext);
                try {
                    server.start();
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.findAndRegisterModules();
                    log.info("Application running...\n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metaData));
                    server.join();
                    return Observable.empty();
                } catch (Throwable throwable) {
                    return Observable.error(throwable);
                }
            })
            .ignoreElements().cast(Void.class);
    }

    private Observable<Void> configureServlets(Map<String, Class<? extends Servlet>> config, Context jettyContext) {
        return Observable.from(config.keySet())
            .zipWith(Observable.from(config.values()),
                (path, servletClass) -> {
                    Servlet injectedClass = context.getInjector().getInstance(servletClass);
                    jettyContext.addServlet(new ServletHolder(injectedClass), path);
                    return null;
                })
            .toList()
            .ignoreElements().cast(Void.class);
    }
}
