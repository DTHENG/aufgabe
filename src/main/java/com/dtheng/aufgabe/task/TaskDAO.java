package com.dtheng.aufgabe.task;

import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.jooq.JooqManager;
import com.dtheng.aufgabe.task.dto.TasksRequest;
import com.dtheng.aufgabe.task.dto.TasksResponse;
import com.dtheng.aufgabe.task.model.Task;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SortOrder;
import org.jooq.Table;
import rx.Observable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
                List<Condition> where = new ArrayList<>();
                if (request.isOnlyShowNeedSync())
                    where.add(
                        field("updatedAt").isNotNull()
                            .and(field("syncedAt").isNull()
                                .or(field("updatedAt").greaterThan(field("syncedAt")))));
                int total = connection.selectCount()
                    .from(TABLE)
                    .where(where)
                    .fetchOne(0, int.class);
                return Observable.from(connection
                    .select()
                    .from(TABLE)
                    .where(where)
                    .orderBy(field("createdAt").sort(SortOrder.DESC))
                    .offset(request.getOffset())
                    .limit(request.getLimit())
                    .fetch())
                    .concatMap(this::toTask)
                    .toList()
                    .map(list -> new TasksResponse(request.getOffset(), request.getLimit(), total, list));
            });
    }

    Observable<Task> setUpdatedAt(String id, Date updatedAt) {
        return jooqManager.getConnection()
            .flatMap(connection -> {
                connection.update(TABLE)
                    .set(field("updatedAt"), updatedAt)
                    .where(field("id").eq(id))
                    .execute();
                return getTask(id);
            });
    }

    Observable<Task> setSyncedAt(String id, Date syncedAt) {
        return jooqManager.getConnection()
            .flatMap(connection -> {
                connection.update(TABLE)
                    .set(field("syncedAt"), syncedAt)
                    .where(field("id").eq(id))
                    .execute();
                return getTask(id);
            });
    }

    private Observable<Task> toTask(Record record) {
        try {
            Date updatedAt = null;
            if (record.getValue("updatedAt") != null)
                updatedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(record.getValue("updatedAt").toString());
            Date syncedAt = null;
            if (record.getValue("syncedAt") != null)
                syncedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(record.getValue("syncedAt").toString());
            return Observable.just(new Task(
                record.getValue("id").toString(),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(record.getValue("createdAt").toString()),
                record.getValue("description").toString(),
                Optional.ofNullable(updatedAt),
                Optional.ofNullable(syncedAt)));
        } catch (Throwable throwable) {
            return Observable.error(throwable);
        }
    }
}