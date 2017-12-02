package com.dtheng.aufgabe.task;

import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.config.model.AufgabeDeviceType;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.exceptions.UnsupportedException;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.util.*;
import com.dtheng.aufgabe.task.dto.*;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class TaskApi {

    public static class Tasks extends AufgabeServlet {

        private TaskManager taskManager;

        @Inject
        public Tasks(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            RequestUtil.getBody(req, TasksRequest.class)
                .defaultIfEmpty(null)
                .flatMap(request -> {
                    if (request == null)
                        return Observable.error(new AufgabeException("Invalid request"));
                    return taskManager.get(request);
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

    public static class GetTask extends AufgabeServlet {

        private TaskManager taskManager;

        @Inject
        public GetTask(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            taskManager.get(req.getPathInfo().substring(1, req.getPathInfo().length()))
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

    public static class CreateTask extends AufgabeServlet {

        private TaskManager taskManager;
        private ConfigManager configManager;

        @Inject
        public CreateTask(TaskManager taskManager, ConfigManager configManager) {
            this.taskManager = taskManager;
            this.configManager = configManager;
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            configManager.getConfig()
                .map(AufgabeConfig::getDeviceType)
                .flatMap(deviceType -> {
                    if (deviceType != AufgabeDeviceType.RASPBERRY_PI)
                        return Observable.error(new UnsupportedException());
                    return RequestUtil.getBody(req, TaskCreateRequest.class)
                        .defaultIfEmpty(null)
                        .flatMap(request -> {
                            if (request == null)
                                return Observable.error(new AufgabeException("Invalid request"));
                            return taskManager.create(request);
                        });
                })
                .defaultIfEmpty(null)
                .flatMap(task -> ResponseUtil.set(resp, Optional.ofNullable(task), 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {},
                    error -> {
                        log.error(error.toString());
                        error.printStackTrace();
                    });
        }
    }

    public static class UpdateTask extends AufgabeServlet {

        private TaskManager taskManager;
        private ConfigManager configManager;

        @Inject
        public UpdateTask(TaskManager taskManager, ConfigManager configManager) {
            this.taskManager = taskManager;
            this.configManager = configManager;
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            configManager.getConfig()
                .map(AufgabeConfig::getDeviceType)
                .flatMap(deviceType -> {
                    if (deviceType != AufgabeDeviceType.RASPBERRY_PI)
                        return Observable.error(new UnsupportedException());
                    return RequestUtil.getBody(req, TaskUpdateRequest.class)
                        .defaultIfEmpty(null)
                        .flatMap(request -> {
                            if (request == null)
                                return Observable.error(new AufgabeException("Invalid request"));
                            return taskManager.update(req.getPathInfo().substring(1, req.getPathInfo().length()), request);
                        });
                })
                .defaultIfEmpty(null)
                .flatMap(task -> ResponseUtil.set(resp, Optional.ofNullable(task), 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {},
                    error -> {
                        log.error(error.toString());
                        error.printStackTrace();
                    });
        }
    }
}
