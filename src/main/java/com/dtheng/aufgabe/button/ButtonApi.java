package com.dtheng.aufgabe.button;

import com.dtheng.aufgabe.button.ButtonManager;
import com.dtheng.aufgabe.button.dto.ButtonCreateRequest;
import com.dtheng.aufgabe.button.dto.ButtonsRequest;
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
public class ButtonApi {

    public static class Buttons extends AufgabeServlet {

        private ButtonManager buttonManager;

        @Inject
        public Buttons(ButtonManager buttonManager) {
            this.buttonManager = buttonManager;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            RequestUtil.getBody(req, ButtonsRequest.class)
                .defaultIfEmpty(null)
                .flatMap(request -> {
                    if (request == null)
                        return Observable.error(new AufgabeException("Invalid request"));
                    return buttonManager.get(request);
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

    public static class GetButton extends AufgabeServlet {

        private ButtonManager buttonManager;

        @Inject
        public GetButton(ButtonManager buttonManager) {
            this.buttonManager = buttonManager;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            buttonManager.get(req.getPathInfo().substring(1, req.getPathInfo().length()))
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

    public static class CreateButton extends AufgabeServlet {

        private ButtonManager buttonManager;
        private ConfigManager configManager;

        @Inject
        public CreateButton(ButtonManager buttonManager, ConfigManager configManager) {
            this.buttonManager = buttonManager;
            this.configManager = configManager;
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            configManager.getConfig()
                .map(Configuration::getDeviceType)
                .flatMap(deviceType -> {
                    if (deviceType != DeviceType.RASPBERRY_PI)
                        return Observable.error(new UnsupportedException());
                    return RequestUtil.getBody(req, ButtonCreateRequest.class)
                        .defaultIfEmpty(null)
                        .flatMap(request -> {
                            if (request == null)
                                return Observable.error(new AufgabeException("Invalid request"));
                            return buttonManager.create(request);
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

    public static class RemoveButton extends AufgabeServlet {

        private ButtonManager buttonManager;
        private ConfigManager configManager;

        @Inject
        public RemoveButton(ButtonManager buttonManager, ConfigManager configManager) {
            this.buttonManager = buttonManager;
            this.configManager = configManager;
        }

        @Override
        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            configManager.getConfig()
                .map(Configuration::getDeviceType)
                .flatMap(deviceType -> {
                    if (deviceType != DeviceType.RASPBERRY_PI)
                        return Observable.error(new UnsupportedException());
                    return buttonManager.remove(req.getPathInfo().substring(1, req.getPathInfo().length()));
                })
                .defaultIfEmpty(null)
                .flatMap(button -> ResponseUtil.set(resp, Optional.ofNullable(button), 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {},
                    error -> {
                        log.error(error.toString());
                        error.printStackTrace();
                    });
        }
    }
}