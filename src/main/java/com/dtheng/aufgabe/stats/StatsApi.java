package com.dtheng.aufgabe.stats;

import com.dtheng.aufgabe.AufgabeContext;
import com.dtheng.aufgabe.input.InputHandler;
import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.input.dto.InputsRequest;
import com.dtheng.aufgabe.input.dto.InputsResponse;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.device.DeviceManager;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.util.ErrorUtil;
import com.dtheng.aufgabe.http.util.ResponseUtil;
import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.stats.dto.StatsDefaultResponse;
import com.dtheng.aufgabe.task.TaskManager;
import com.dtheng.aufgabe.task.dto.AggregateTask;
import com.dtheng.aufgabe.task.dto.AggregateTasksResponse;
import com.dtheng.aufgabe.task.dto.TasksRequest;
import com.dtheng.aufgabe.task.dto.TasksResponse;
import com.dtheng.aufgabe.taskentry.TaskEntryManager;
import com.dtheng.aufgabe.taskentry.dto.EntriesRequest;
import com.dtheng.aufgabe.taskentry.dto.EntriesResponse;
import com.dtheng.aufgabe.taskentry.model.TaskEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class StatsApi {

    public static class Default extends AufgabeServlet {

        private DeviceManager deviceManager;
        private InputManager inputManager;
        private TaskManager taskManager;
        private TaskEntryManager taskEntryManager;
        private ConfigManager configManager;
        private AufgabeContext aufgabeContext;

        @Inject
        public Default(DeviceManager deviceManager, InputManager inputManager, TaskManager taskManager, TaskEntryManager taskEntryManager, ConfigManager configManager, AufgabeContext aufgabeContext) {
            this.deviceManager = deviceManager;
            this.inputManager = inputManager;
            this.taskManager = taskManager;
            this.taskEntryManager = taskEntryManager;
            this.configManager = configManager;
            this.aufgabeContext = aufgabeContext;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            Observable.zip(
                    getListOfInputs().toList(),
                    taskEntryManager.get(new EntriesRequest())
                        .map(EntriesResponse::getEntries)
                        .flatMap(taskEntries -> Observable.from(taskEntries)
                            .flatMap(entry -> taskManager.get(entry.getTaskId())
                                .flatMap(task -> toAggregateTaskEntry(entry, task)))
                            .toList()),
                    buildTotalsMap(),
                StatsDefaultResponse::new)
                .defaultIfEmpty(null)
                .flatMap(body -> ResponseUtil.set(resp, Optional.ofNullable(body), 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {},
                    error -> {
                        log.error(error.toString());
                        error.printStackTrace();
                    });
        }

        private Observable<Map<String, Integer>> buildTotalsMap() {
            return Observable.zip(taskEntryManager.get(new EntriesRequest()).map(EntriesResponse::getTotal),
                taskManager.get(new TasksRequest()).map(AggregateTasksResponse::getTotal),
                inputManager.get(new InputsRequest()).map(InputsResponse::getTotal),
                (totalEntries, totalTasks, totalInputs) -> {
                    Map<String, Integer> totals = new HashMap<>();
                    totals.put("entries", totalEntries);
                    totals.put("tasks", totalTasks);
                    totals.put("inputs", totalInputs);
                    return totals;
                });
        }

        private Observable<JsonNode> toAggregateTaskEntry(TaskEntry entry, AggregateTask task) {
            return Observable.zip(
                configManager.getConfig(),
                inputManager.get(entry.getInputId())
                    .map(Input::getHandler)
                    .map(aufgabeContext.getInjector()::getInstance)
                    .map(InputHandler::getName),
                (configuration, handlerName) -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.findAndRegisterModules();
                    ObjectNode node = objectMapper.valueToTree(entry);
                    node.remove("taskId");
                    node.remove("inputId");
                    node.remove("createdAt");
                    node.remove("updatedAt");
                    node.remove("syncedAt");
                    node.put("task", task.getTask().getDescription() +" ("+ handlerName +")");
                    long timeZoneOffset = TimeZone.getTimeZone(configuration.getTimeZone()).getRawOffset();
                    Date adjustedDate = new Date(entry.getCreatedAt().getTime() + timeZoneOffset);
                    node.put("createdAt", new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(adjustedDate));
                    return (JsonNode) node;
                });
        }

        private Observable<String> getListOfInputs() {
            return deviceManager.getDeviceId()
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