package com.dtheng.aufgabe.task;

import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.jooq.JooqService;
import com.dtheng.aufgabe.task.dto.TaskUpdateRequest;
import com.dtheng.aufgabe.task.dto.TasksRequest;
import com.dtheng.aufgabe.task.dto.TasksResponse;
import com.dtheng.aufgabe.task.model.Task;
import com.dtheng.aufgabe.util.DateUtil;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import rx.Observable;

import java.util.*;

import static org.jooq.impl.DSL.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
class TaskDAO {

    private static final Table<Record> TABLE = table("task");

    private JooqService jooqService;

    @Inject
    public TaskDAO(JooqService jooqService) {
        this.jooqService = jooqService;
    }

    Observable<Task> createTask(Task task) {
        return jooqService.getConnection()
            .doOnNext(connection -> connection.insertInto(TABLE)
                .set(field("id"), task.getId())
                .set(field("description"), task.getDescription())
                .set(field("bonuslyMessage"), task.getBonuslyMessage().orElseGet(() -> null))
                .execute())
            .flatMap(Void -> getTask(task.getId()));
    }

    Observable<Task> getTask(String id) {
        return jooqService.getConnection()
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
        return jooqService.getConnection()
            .flatMap(connection -> {
                List<Condition> where = new ArrayList<>();
                if (request.isOnlyShowNeedSync())
                    where.add(
                        field("updatedAt").isNull()
                            .and(field("syncedAt").isNull()
                                .or(field("updatedAt").isNotNull()
                                    .and(field("updatedAt").greaterThan(field("syncedAt"))))));
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
        return jooqService.getConnection()
            .flatMap(connection -> {
                connection.update(TABLE)
                    .set(field("updatedAt"), updatedAt)
                    .where(field("id").eq(id))
                    .execute();
                return getTask(id);
            });
    }

    Observable<Task> setSyncedAt(String id, Date syncedAt) {
        return jooqService.getConnection()
            .flatMap(connection -> {
                connection.update(TABLE)
                    .set(field("syncedAt"), syncedAt)
                    .where(field("id").eq(id))
                    .execute();
                return getTask(id);
            });
    }

    Observable<Void> update(String id, TaskUpdateRequest request) {
        Map<Field, String> set = new HashMap<>();
        if (request.getDescription().isPresent())
            set.put(field("description"), request.getDescription().get());
        if (request.getBonuslyMessage().isPresent())
            set.put(field("bonuslyMessage"), request.getBonuslyMessage().get());
        return jooqService.getConnection()
            .doOnNext(connection -> connection.update(TABLE)
                .set(set)
                .where(field("id").eq(id))
            .execute())
            .ignoreElements().cast(Void.class);
    }

    private Observable<Task> toTask(Record record) {
        Observable<Date> oCreatedAt = DateUtil.parse(record.getValue("createdAt").toString());
        Observable<Date> oUpdatedAt = Observable.empty();
        Observable<Date> oSyncedAt = Observable.empty();
        if (record.getValue("updatedAt") != null)
            oUpdatedAt = DateUtil.parse(record.getValue("updatedAt").toString());
        if (record.getValue("syncedAt") != null)
            oSyncedAt = DateUtil.parse(record.getValue("syncedAt").toString());
        Optional<String> bonuslyMessage = record.getValue("bonuslyMessage") != null ? Optional.of(record.getValue("bonuslyMessage").toString()) : Optional.empty();
        return Observable.zip(oCreatedAt, oUpdatedAt.defaultIfEmpty(null), oSyncedAt.defaultIfEmpty(null),
            (createdAt, updatedAt, syncedAt) ->
                new Task(
                    record.getValue("id").toString(),
                    createdAt,
                    record.getValue("description").toString(),
                    Optional.ofNullable(updatedAt),
                    Optional.ofNullable(syncedAt),
                    bonuslyMessage));
    }
}