package com.dtheng.aufgabe.device;

import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.device.dto.DeviceCreateRequest;
import com.dtheng.aufgabe.device.dto.DevicesRequest;
import com.dtheng.aufgabe.exceptions.UnsupportedException;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.HttpManager;
import com.dtheng.aufgabe.http.util.ErrorUtil;
import com.dtheng.aufgabe.http.util.RequestUtil;
import com.dtheng.aufgabe.http.util.ResponseUtil;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class DeviceApi {


    public static class Devices extends AufgabeServlet {

        private DeviceManager deviceManager;

        @Inject
        public Devices(DeviceManager deviceManager) {
            this.deviceManager = deviceManager;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            RequestUtil.getBody(req, DevicesRequest.class)
                .flatMap(deviceManager::get)
                .flatMap(entries -> ResponseUtil.set(resp, entries, 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {}, error -> log.error(error.toString()));
        }
    }

    public static class GetDevice extends AufgabeServlet {

        private DeviceManager deviceManager;

        @Inject
        public GetDevice(DeviceManager deviceManager) {
            this.deviceManager = deviceManager;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            deviceManager.get(req.getPathInfo().substring(1, req.getPathInfo().length()))
                .flatMap(device -> ResponseUtil.set(resp, device, 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {}, error -> log.error(error.toString()));
        }
    }

    public static class CreateCreate extends AufgabeServlet {

        private DeviceManager deviceManager;
        private ConfigManager configManager;
        private HttpManager httpManager;

        @Inject
        public CreateCreate(DeviceManager deviceManager, ConfigManager configManager, HttpManager httpManager) {
            this.deviceManager = deviceManager;
            this.configManager = configManager;
            this.httpManager = httpManager;
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            configManager.getConfig()
                .map(AufgabeConfig::getDeviceType)
                .filter(deviceType -> {
                    switch (deviceType) {
                        case RASPBERRY_PI:
                        case MAC_OS:
                            return true;
                        default:
                            throw new UnsupportedException();
                    }
                })
                .flatMap(Void -> httpManager.getBody(req, DeviceCreateRequest.class))
                .flatMap(deviceManager::create)
                .flatMap(input -> ResponseUtil.set(resp, input, 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {}, error -> log.error(error.toString()));
        }
    }
}
