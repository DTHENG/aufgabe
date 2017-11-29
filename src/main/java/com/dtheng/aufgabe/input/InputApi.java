package com.dtheng.aufgabe.input;

import com.dtheng.aufgabe.input.dto.InputCreateRequest;
import com.dtheng.aufgabe.input.dto.InputsRequest;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.Configuration;
import com.dtheng.aufgabe.config.model.DeviceType;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.exceptions.UnsupportedException;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.util.ErrorUtil;
import com.dtheng.aufgabe.http.util.RequestUtil;
import com.dtheng.aufgabe.http.util.ResponseUtil;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class InputApi {

    public static class Inputs extends AufgabeServlet {

        private InputManager inputManager;

        @Inject
        public Inputs(InputManager inputManager) {
            this.inputManager = inputManager;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            RequestUtil.getBody(req, InputsRequest.class)
                .defaultIfEmpty(null)
                .flatMap(request -> {
                    if (request == null)
                        return Observable.error(new AufgabeException("Invalid request"));
                    return inputManager.get(request);
                })
                .defaultIfEmpty(null)
                .flatMap(entries -> ResponseUtil.set(resp, Optional.ofNullable(entries), 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {},
                    error -> {
                        log.error(error.toString());
                        error.printStackTrace();
                    });
        }
    }

    public static class GetInput extends AufgabeServlet {

        private InputManager inputManager;

        @Inject
        public GetInput(InputManager inputManager) {
            this.inputManager = inputManager;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            inputManager.get(req.getPathInfo().substring(1, req.getPathInfo().length()))
                .defaultIfEmpty(null)
                .flatMap(taskEntry -> ResponseUtil.set(resp, Optional.ofNullable(taskEntry), 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {},
                    error -> {
                        log.error(error.toString());
                        error.printStackTrace();
                    });
        }
    }

    public static class CreateCreate extends AufgabeServlet {

        private InputManager inputManager;
        private ConfigManager configManager;

        @Inject
        public CreateCreate(InputManager inputManager, ConfigManager configManager) {
            this.inputManager = inputManager;
            this.configManager = configManager;
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            configManager.getConfig()
                .map(Configuration::getDeviceType)
                .flatMap(deviceType -> {
                    if (deviceType != DeviceType.RASPBERRY_PI)
                        return Observable.error(new UnsupportedException());
                    return RequestUtil.getBody(req, InputCreateRequest.class)
                        .defaultIfEmpty(null)
                        .flatMap(request -> {
                            if (request == null)
                                return Observable.error(new AufgabeException("Invalid request"));
                            return inputManager.create(request);
                        });
                })
                .defaultIfEmpty(null)
                .flatMap(entries -> ResponseUtil.set(resp, Optional.ofNullable(entries), 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {},
                    error -> {
                        log.error(error.toString());
                        error.printStackTrace();
                    });
        }
    }

    public static class RemoveInput extends AufgabeServlet {

        private InputManager inputManager;
        private ConfigManager configManager;

        @Inject
        public RemoveInput(InputManager inputManager, ConfigManager configManager) {
            this.inputManager = inputManager;
            this.configManager = configManager;
        }

        @Override
        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            configManager.getConfig()
                .map(Configuration::getDeviceType)
                .flatMap(deviceType -> {
                    if (deviceType != DeviceType.RASPBERRY_PI)
                        return Observable.error(new UnsupportedException());
                    return inputManager.remove(req.getPathInfo().substring(1, req.getPathInfo().length()));
                })
                .defaultIfEmpty(null)
                .flatMap(input -> ResponseUtil.set(resp, Optional.ofNullable(input), 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {},
                    error -> {
                        log.error(error.toString());
                        error.printStackTrace();
                    });
        }
    }
}