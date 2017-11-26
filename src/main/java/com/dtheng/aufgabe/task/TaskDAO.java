package com.dtheng.aufgabe.task;

import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.jooq.JooqManager;
import com.dtheng.aufgabe.task.dto.TasksRequest;
import com.dtheng.aufgabe.task.dto.TasksResponse;
import com.dtheng.aufgabe.task.model.Task;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.SortOrder;
import org.jooq.Table;
import rx.Observable;

import java.text.SimpleDateFormat;

import static org.jooq.impl.DSL.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
class TaskDAO {

    private static final Table<Record> TABLE = table("task");

    private JooqManager jooqManager;

    @Inject
    public TaskDAO(JooqManager jooqManager) {
        this.jooqManager = jooqManager;
    }

    Observable<Task> createTask(Task task) {
        return jooqManager.getConnection()
            .doOnNext(connection -> connection.insertInto(TABLE)
                .set(field("id"), task.getId())
                .set(field("description"), task.getDescription())
                .execute())
            .flatMap(Void -> getTask(task.getId()));
    }

    Observable<Task> getTask(String id) {
        return jooqManager.getConnection()
            .flatMap(connection -> Observable.from(connection.select()
                .from(TABLE)
                .where(field("id").eq(id))
                .fetch()))
            .defaultIfEmpty(null)
            .flatMap(record -> {
                if (record == null) {
                    log.error("Task not found, id: {}", id);
                    return Observable.error(new AufgabeException("Task not found"));
                }
                return Observable.just(record);
            })
            .flatMap(this::toTask);
    }

    Observable<TasksResponse> getTasks(TasksRequest request) {
        return jooqManager.getConnection()
            .flatMap(connection -> {
                int total = connection.selectCount()
                    .from(TABLE)
                    .fetchOne(0, int.class);
                return Observable.from(connection
                    .select()
                    .from(TABLE)
                    .orderBy(field("createdAt").sort(SortOrder.DESC))
                    .offset(request.getOffset())
                    .limit(request.getLimit())
                    .fetch())
                    .concatMap(this::toTask)
                    .toList()
                    .map(list -> new TasksResponse(request.getOffset(), request.getLimit(), total, list));
            });
    }

    private Observable<Task> toTask(Record record) {
        try {
            return Observable.just(new Task(
                record.getValue("id").toString(),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(record.getValue("createdAt").toString()),
                record.getValue("description").toString()));
        } catch (Throwable throwable) {
            return Observable.error(throwable);
        }
    }
}