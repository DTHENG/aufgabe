package com.dtheng.aufgabe.config;

import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.input.dto.InputsRequest;
import com.dtheng.aufgabe.input.dto.InputsResponse;
import com.dtheng.aufgabe.device.DeviceService;
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

    public static class InputConfig extends AufgabeServlet {

        private InputManager inputManager;
        private DeviceService deviceService;
        private TaskManager taskManager;

        @Inject
        public InputConfig(InputManager inputManager, DeviceService deviceService, TaskManager taskManager) {
            this.inputManager = inputManager;
            this.deviceService = deviceService;
            this.taskManager = taskManager;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            getTasks()
                .toList()
                .flatMap(tasks -> ResponseUtil.set(resp, tasks, 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {}, error -> log.error(error.toString()));
        }

        private Observable<String> getTasks() {
            return deviceService.getDeviceId()
                .flatMap(deviceId -> {
                    InputsRequest inputsRequest = new InputsRequest();
                    inputsRequest.setLimit(100);
                    inputsRequest.setDevice(Optional.of(deviceId));
                    inputsRequest.setOrderBy(Optional.of("ioPin"));
                    inputsRequest.setOrderDirection(Optional.of("asc"));
                    return inputManager.get(inputsRequest);
                })
                .map(InputsResponse::getInputs)
                .flatMap(Observable::from)
                .concatMap(input -> taskManager.get(input.getTaskId())
                    .map(task -> task.getTask().getDescription()));
        }
    }
}