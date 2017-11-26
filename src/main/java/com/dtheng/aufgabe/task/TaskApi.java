package com.dtheng.aufgabe.task;

import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.util.ErrorUtil;
import com.dtheng.aufgabe.http.util.RequestUtil;
import com.dtheng.aufgabe.http.util.ResponseUtil;
import com.dtheng.aufgabe.task.TaskManager;
import com.dtheng.aufgabe.task.dto.TaskCreateRequest;
import com.dtheng.aufgabe.task.dto.TasksRequest;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
                    .flatMap(entries -> ResponseUtil.set(resp, entries, 200))
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
                    .flatMap(taskEntry -> ResponseUtil.set(resp, taskEntry, 200))
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

        @Inject
        public CreateTask(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            RequestUtil.getBody(req, TaskCreateRequest.class)
                    .defaultIfEmpty(null)
                    .flatMap(request -> {
                        if (request == null)
                            return Observable.error(new AufgabeException("Invalid request"));
                        return taskManager.create(request);
                    })
                    .flatMap(entries -> ResponseUtil.set(resp, entries, 200))
                    .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                    .subscribe(Void -> {},
                            error -> {
                                log.error(error.toString());
                                error.printStackTrace();
                            });
        }
    }
}
