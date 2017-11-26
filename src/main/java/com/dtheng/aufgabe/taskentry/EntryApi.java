package com.dtheng.aufgabe.taskentry;

import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.http.AufgabeServlet;
import com.dtheng.aufgabe.http.util.ErrorUtil;
import com.dtheng.aufgabe.http.util.RequestUtil;
import com.dtheng.aufgabe.http.util.ResponseUtil;
import com.dtheng.aufgabe.taskentry.dto.EntriesRequest;
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
public class EntryApi {

    public static class Entries extends AufgabeServlet {

        private TaskEntryManager taskEntryManager;

        @Inject
        public Entries(TaskEntryManager taskEntryManager) {
            this.taskEntryManager = taskEntryManager;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            RequestUtil.getBody(req, EntriesRequest.class)
                .defaultIfEmpty(null)
                .flatMap(request -> {
                    if (request == null)
                        return Observable.error(new AufgabeException("Invalid request"));
                    return taskEntryManager.get(request);
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

    public static class GetEntry extends AufgabeServlet {

        private TaskEntryManager taskEntryManager;

        @Inject
        public GetEntry(TaskEntryManager taskEntryManager) {
            this.taskEntryManager = taskEntryManager;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            taskEntryManager.get(req.getPathInfo().substring(1, req.getPathInfo().length()))
                .flatMap(taskEntry -> ResponseUtil.set(resp, taskEntry, 200))
                .onErrorResumeNext(throwable -> ErrorUtil.handle(throwable, resp))
                .subscribe(Void -> {},
                    error -> {
                        log.error(error.toString());
                        error.printStackTrace();
                    });
        }
    }
}