package com.dtheng.aufgabe.sync;

import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.config.model.AufgabeDeviceType;
import com.dtheng.aufgabe.exceptions.UnsupportedException;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.util.ErrorUtil;
import com.dtheng.aufgabe.http.util.RequestUtil;
import com.dtheng.aufgabe.http.util.ResponseUtil;
import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.input.dto.InputCreateRequest;
import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.task.TaskManager;
import com.dtheng.aufgabe.task.dto.TaskCreateRequest;
import com.dtheng.aufgabe.task.model.Task;
import com.dtheng.aufgabe.taskentry.TaskEntryManager;
import com.dtheng.aufgabe.taskentry.dto.TaskEntryCreateRequest;
import com.dtheng.aufgabe.taskentry.model.TaskEntry;
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
public class SyncApi {

    public static class SyncTask extends AufgabeServlet {

        private TaskManager taskManager;
        private ConfigManager configManager;

        @Inject
        public SyncTask(TaskManager taskManager, ConfigManager configManager) {
            this.taskManager = taskManager;
            this.configManager = configManager;
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            configManager.getConfig()
                .map(AufgabeConfig::getDeviceType)
                .flatMap(deviceType -> {
                    if (deviceType != AufgabeDeviceType.EC2_INSTANCE)
                        return Observable.error(new UnsupportedException());
                    return RequestUtil.getBody(req, Task.class);
                })
                .flatMap(task -> {
                    if (task.getSyncedAt().isPresent()) {

                        // TODO: need to modify the task here...

                        throw new RuntimeException("Unimplemented");
                    } else {
                        return taskManager.create(new TaskCreateRequest(task.getDescription(), Optional.of(task.getId()), Optional.of(task.getCreatedAt())));
                    }
                })
                .flatMap(Void -> ResponseUtil.set(resp, Optional.empty(), 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {},
                    error -> {
                        log.error(error.toString());
                        error.printStackTrace();
                    });
        }
    }

    public static class SyncEntry extends AufgabeServlet {

        private TaskEntryManager taskEntryManager;
        private ConfigManager configManager;

        @Inject
        public SyncEntry(TaskEntryManager taskEntryManager, ConfigManager configManager) {
            this.taskEntryManager = taskEntryManager;
            this.configManager = configManager;
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            configManager.getConfig()
                .map(AufgabeConfig::getDeviceType)
                .flatMap(deviceType -> {
                    if (deviceType != AufgabeDeviceType.EC2_INSTANCE)
                        return Observable.error(new UnsupportedException());
                    return RequestUtil.getBody(req, TaskEntry.class);
                })
                .flatMap(taskEntry -> {
                    if (taskEntry.getSyncedAt().isPresent()) {

                        // TODO: need to modify the task entry here...

                        throw new RuntimeException("Unimplemented");
                    } else {
                        return taskEntryManager.create(new TaskEntryCreateRequest(taskEntry.getTaskId(), taskEntry.getInputId(),
                            Optional.of(taskEntry.getId()), Optional.of(taskEntry.getCreatedAt())));
                    }
                })
                .flatMap(Void -> ResponseUtil.set(resp, Optional.empty(), 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {},
                    error -> {
                        log.error(error.toString());
                        error.printStackTrace();
                    });
        }
    }

    public static class SyncInput extends AufgabeServlet {

        private InputManager inputManager;
        private ConfigManager configManager;

        @Inject
        public SyncInput(InputManager inputManager, ConfigManager configManager) {
            this.inputManager = inputManager;
            this.configManager = configManager;
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            configManager.getConfig()
                .map(AufgabeConfig::getDeviceType)
                .flatMap(deviceType -> {
                    if (deviceType != AufgabeDeviceType.EC2_INSTANCE)
                        return Observable.error(new UnsupportedException());
                    return RequestUtil.getBody(req, Input.class);
                })
                .flatMap(input -> {
                    if (input.getSyncedAt().isPresent()) {

                        // TODO: need to modify the input here...

                        throw new RuntimeException("Unimplemented");
                    } else {
                        return inputManager.create(new InputCreateRequest(input.getIoPin(), input.getTaskId(), input.getHandler().getCanonicalName(), Optional.of(input.getCreatedAt()), Optional.of(input.getId()), Optional.of(input.getDevice())));
                    }
                })
                .flatMap(Void -> ResponseUtil.set(resp, Optional.empty(), 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {},
                    error -> {
                        log.error(error.toString());
                        error.printStackTrace();
                    });
        }
    }
}
