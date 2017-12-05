package com.dtheng.aufgabe.stats;

import com.dtheng.aufgabe.AufgabeContext;
import com.dtheng.aufgabe.device.DeviceManager;
import com.dtheng.aufgabe.device.dto.DevicesRequest;
import com.dtheng.aufgabe.device.dto.DevicesResponse;
import com.dtheng.aufgabe.device.model.Device;
import com.dtheng.aufgabe.input.InputHandler;
import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.input.dto.InputsRequest;
import com.dtheng.aufgabe.input.dto.InputsResponse;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.util.ErrorUtil;
import com.dtheng.aufgabe.http.util.ResponseUtil;
import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.stats.dto.StatsDefaultResponse;
import com.dtheng.aufgabe.task.TaskManager;
import com.dtheng.aufgabe.task.dto.TasksRequest;
import com.dtheng.aufgabe.task.dto.TasksResponse;
import com.dtheng.aufgabe.task.model.Task;
import com.dtheng.aufgabe.taskentry.TaskEntryManager;
import com.dtheng.aufgabe.taskentry.dto.EntriesRequest;
import com.dtheng.aufgabe.taskentry.dto.EntriesResponse;
import com.dtheng.aufgabe.taskentry.model.TaskEntry;
import com.dtheng.aufgabe.util.DateUtil;
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
import java.util.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class StatsApi {

    public static class Default extends AufgabeServlet {

        private InputManager inputManager;
        private TaskManager taskManager;
        private TaskEntryManager taskEntryManager;
        private ConfigManager configManager;
        private AufgabeContext aufgabeContext;
        private DeviceManager deviceManager;

        @Inject
        public Default(InputManager inputManager, TaskManager taskManager, TaskEntryManager taskEntryManager, ConfigManager configManager, AufgabeContext aufgabeContext, DeviceManager deviceManager) {
            this.inputManager = inputManager;
            this.taskManager = taskManager;
            this.taskEntryManager = taskEntryManager;
            this.configManager = configManager;
            this.aufgabeContext = aufgabeContext;
            this.deviceManager = deviceManager;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            EntriesRequest entriesRequest = new EntriesRequest();
            entriesRequest.setLimit(5);
            Observable.zip(
                buildDevicesList().toList(),
                buildTotalsMap(),
                taskEntryManager.get(entriesRequest)
                    .map(EntriesResponse::getEntries)
                    .flatMap(taskEntries -> Observable.from(taskEntries)
                        .flatMap(entry -> taskManager.get(entry.getTaskId())
                            .flatMap(task -> toAggregateTaskEntry(entry, task)))
                        .toList()),
                StatsDefaultResponse::new)
                .flatMap(body -> ResponseUtil.set(resp, body, 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {}, error -> log.error(error.toString()));
        }

        private Observable<Device> buildDevicesList() {
            return deviceManager.get(new DevicesRequest())
                .flatMap(devicesResponse -> Observable.from(devicesResponse.getDevices()));
        }

        private Observable<Map<String, Integer>> buildTotalsMap() {
            return Observable.zip(
                taskEntryManager.get(new EntriesRequest()).map(EntriesResponse::getTotal),
                taskManager.get(new TasksRequest()).map(TasksResponse::getTotal),
                inputManager.get(new InputsRequest()).map(InputsResponse::getTotal),
                deviceManager.get(new DevicesRequest()).map(DevicesResponse::getTotal),
                (totalEntries, totalTasks, totalInputs, totalDevices) -> {
                    Map<String, Integer> totals = new HashMap<>();
                    totals.put("entry", totalEntries);
                    totals.put("task", totalTasks);
                    totals.put("input", totalInputs);
                    totals.put("deviceId", totalDevices);
                    return totals;
                });
        }

        private Observable<JsonNode> toAggregateTaskEntry(TaskEntry entry, Task task) {
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
                    node.put("task", task.getDescription());
                    node.put("source", handlerName);
                    long timeZoneOffset = TimeZone.getTimeZone(configuration.getTimeZone()).getRawOffset();
                    Date adjustedDate = new Date(entry.getCreatedAt().getTime() + timeZoneOffset);
                    node.put("createdAt", DateUtil.toString(adjustedDate));
                    return (JsonNode) node;
                });
        }
    }
}