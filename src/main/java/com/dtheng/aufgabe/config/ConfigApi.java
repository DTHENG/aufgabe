package com.dtheng.aufgabe.config;

import com.dtheng.aufgabe.button.ButtonManager;
import com.dtheng.aufgabe.button.dto.ButtonsRequest;
import com.dtheng.aufgabe.button.dto.ButtonsResponse;
import com.dtheng.aufgabe.device.DeviceManager;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.util.ErrorUtil;
import com.dtheng.aufgabe.http.util.ResponseUtil;
import com.dtheng.aufgabe.task.TaskManager;
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
public class ConfigApi {

    public static class ButtonConfig extends AufgabeServlet {

        private ButtonManager buttonManager;
        private DeviceManager deviceManager;
        private TaskManager taskManager;

        @Inject
        public ButtonConfig(ButtonManager buttonManager, DeviceManager deviceManager, TaskManager taskManager) {
            this.buttonManager = buttonManager;
            this.deviceManager = deviceManager;
            this.taskManager = taskManager;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            deviceManager.getDeviceId()
                    .flatMap(deviceId -> {
                        ButtonsRequest buttonsRequest = new ButtonsRequest();
                        buttonsRequest.setLimit(100);
                        buttonsRequest.setDevice(Optional.of(deviceId));
                        buttonsRequest.setOrderBy(Optional.of("ioPin"));
                        buttonsRequest.setOrderDirection(Optional.of("asc"));
                        return buttonManager.get(buttonsRequest);
                    })
                    .map(ButtonsResponse::getButtons)
                    .flatMap(Observable::from)
                    .concatMap(button -> taskManager.get(button.getTaskId())
                        .map(task -> task.getTask().getDescription()))
                    .toList()
                    .flatMap(tasks -> ResponseUtil.set(resp, tasks, 200))
                    .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                    .subscribe(Void -> {},
                            error -> {
                                log.error(error.toString());
                                error.printStackTrace();
                            });
        }
    }
}
