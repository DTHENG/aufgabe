package com.dtheng.aufgabe.input;

import com.dtheng.aufgabe.input.dto.InputCreateRequest;
import com.dtheng.aufgabe.input.dto.InputsRequest;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.config.model.AufgabeDeviceType;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.exceptions.UnsupportedException;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.util.ErrorUtil;
import com.dtheng.aufgabe.http.util.RequestUtil;
import com.dtheng.aufgabe.http.util.ResponseUtil;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

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
                .map(Optional::ofNullable)
                .filter(inputsRequest -> {
                    if ( ! inputsRequest.isPresent())
                        throw new AufgabeException("Invalid request");
                    return true;
                })
                .map(Optional::get)
                .flatMap(inputManager::get)
                .flatMap(entries -> ResponseUtil.set(resp, entries, 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {}, error -> log.error(error.toString()));
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
                .flatMap(taskEntry -> ResponseUtil.set(resp, taskEntry, 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {}, error -> log.error(error.toString()));
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
                .map(AufgabeConfig::getDeviceType)
                .filter(deviceType -> {
                    if (deviceType != AufgabeDeviceType.RASPBERRY_PI)
                        throw new UnsupportedException();
                    return true;
                })
                .flatMap(Void -> RequestUtil.getBody(req, InputCreateRequest.class)
                    .defaultIfEmpty(null))
                .map(Optional::ofNullable)
                .filter(inputCreateRequest -> {
                    if ( ! inputCreateRequest.isPresent())
                        throw new AufgabeException("Invalid request");
                    return true;
                })
                .map(Optional::get)
                .flatMap(inputManager::create)
                .flatMap(input -> ResponseUtil.set(resp, input, 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {}, error -> log.error(error.toString()));
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
                .map(AufgabeConfig::getDeviceType)
                .filter(deviceType -> {
                    if (deviceType != AufgabeDeviceType.RASPBERRY_PI)
                        throw new UnsupportedException();
                    return true;
                })
                .flatMap(Void -> inputManager.remove(req.getPathInfo().substring(1, req.getPathInfo().length())))
                .flatMap(removedInput -> ResponseUtil.set(resp, removedInput, 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {}, error -> log.error(error.toString()));
        }
    }
}