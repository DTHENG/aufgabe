package com.dtheng.aufgabe.task;

import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.exceptions.UnsupportedException;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.HttpManager;
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
                .flatMap(taskManager::get)
                .flatMap(entries -> ResponseUtil.set(resp, entries, 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {}, error -> log.error(error.toString()));
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
                .flatMap(taskEntry -> ResponseUtil.set(resp, taskEntry, 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {}, error -> log.error(error.toString()));
        }
    }

    public static class CreateTask extends AufgabeServlet {

        private TaskManager taskManager;
        private ConfigManager configManager;
        private HttpManager httpManager;

        @Inject
        public CreateTask(TaskManager taskManager, ConfigManager configManager, HttpManager httpManager) {
            this.taskManager = taskManager;
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
                .flatMap(deviceType -> httpManager.getBody(req, TaskCreateRequest.class)
                    .defaultIfEmpty(null)
                    .flatMap(request -> {
                        if (request == null)
                            return Observable.error(new AufgabeException("Invalid request"));
                        return taskManager.create(request);
                    }))
                .defaultIfEmpty(null)
                .flatMap(task -> ResponseUtil.set(resp, Optional.ofNullable(task), 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {}, error -> log.error(error.toString()));
        }
    }

    public static class UpdateTask extends AufgabeServlet {

        private TaskManager taskManager;
        private ConfigManager configManager;
        private HttpManager httpManager;

        @Inject
        public UpdateTask(TaskManager taskManager, ConfigManager configManager, HttpManager httpManager) {
            this.taskManager = taskManager;
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
                .flatMap(deviceType -> httpManager.getBody(req, TaskUpdateRequest.class)
                    .defaultIfEmpty(null)
                    .flatMap(request -> {
                        if (request == null)
                            return Observable.error(new AufgabeException("Invalid request"));
                        return taskManager.update(req.getPathInfo().substring(1, req.getPathInfo().length()), request);
                    }))
                .flatMap(task -> ResponseUtil.set(resp, task, 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {}, error -> log.error(error.toString()));
        }
    }
}
