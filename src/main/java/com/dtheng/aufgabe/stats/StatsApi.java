package com.dtheng.aufgabe.stats;

import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.input.dto.InputsRequest;
import com.dtheng.aufgabe.input.dto.InputsResponse;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.device.DeviceManager;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.util.ErrorUtil;
import com.dtheng.aufgabe.http.util.ResponseUtil;
import com.dtheng.aufgabe.stats.dto.StatsDefaultResponse;
import com.dtheng.aufgabe.task.TaskManager;
import com.dtheng.aufgabe.task.dto.AggregateTask;
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
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

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

        @Inject
        public Default(DeviceManager deviceManager, InputManager inputManager, TaskManager taskManager, TaskEntryManager taskEntryManager, ConfigManager configManager) {
            this.deviceManager = deviceManager;
            this.inputManager = inputManager;
            this.taskManager = taskManager;
            this.taskEntryManager = taskEntryManager;
            this.configManager = configManager;
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

        private Observable<JsonNode> toAggregateTaskEntry(TaskEntry entry, AggregateTask task) {
            return configManager.getConfig()
                .map(configuration -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ObjectNode node = objectMapper.valueToTree(entry);
                    node.remove("taskId");
                    node.remove("createdAt");
                    node.put("task", task.getTask().getDescription());
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