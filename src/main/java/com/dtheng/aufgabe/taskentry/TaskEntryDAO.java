package com.dtheng.aufgabe.taskentry;

import com.dtheng.aufgabe.jooq.JooqService;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.taskentry.dto.EntriesRequest;
import com.dtheng.aufgabe.taskentry.dto.EntriesResponse;
import com.dtheng.aufgabe.taskentry.model.TaskEntry;
import com.dtheng.aufgabe.util.DateUtil;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SortOrder;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import rx.Observable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
class TaskEntryDAO {

    private static final Table<Record> TABLE = table("task_entry");

    private JooqService jooqService;

    @Inject
    public TaskEntryDAO(JooqService jooqService) {
        this.jooqService = jooqService;
    }

    Observable<TaskEntry> createTaskEntry(TaskEntry entry) {
        return jooqService.getConnection()
            .doOnNext(connection -> connection.insertInto(TABLE)
                .set(field("id"), entry.getId())
                .set(field("createdAt"), entry.getCreatedAt())
                .set(field("taskId"), entry.getTaskId())
                .set(field("inputId"), entry.getInputId())
                .execute())
            .flatMap(Void -> getTaskEntry(entry.getId()))
            .onErrorResumeNext(error -> {
                if (error instanceof DataAccessException) {
                    log.error(error.getLocalizedMessage());
                    return Observable.error(new AufgabeException("Unknown error"));
                }
                return Observable.error(error);
            });
    }

    Observable<TaskEntry> getTaskEntry(String id) {
        return jooqService.getConnection()
            .flatMap(connection -> Observable.from(connection.select()
                .from(TABLE)
                .where(field("id").eq(id))
                .fetch()))
            .defaultIfEmpty(null)
            .flatMap(record -> {
                if (record == null) {
                    log.error("Task entry not found, id: {}", id);
                    return Observable.error(new AufgabeException("Task entry not found"));
                }
                return Observable.just(record);
            })
            .flatMap(this::toTaskEntry);
    }

    Observable<EntriesResponse> getEntries(EntriesRequest request) {
        return jooqService.getConnection()
            .flatMap(connection -> {
                List<Condition> where = new ArrayList<>();

                if (request.getTaskId().isPresent())
                    where.add(field("taskId").eq(request.getTaskId().get()));
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
                    .concatMap(this::toTaskEntry)
                    .toList()
                    .map(list -> new EntriesResponse(request.getOffset(), request.getLimit(), total, list));
            });
    }

    Observable<TaskEntry> setUpdatedAt(String id, Date updatedAt) {
        return jooqService.getConnection()
            .flatMap(connection -> {
                connection.update(TABLE)
                    .set(field("updatedAt"), updatedAt)
                    .where(field("id").eq(id))
                    .execute();
                return getTaskEntry(id);
            });
    }

    Observable<TaskEntry> setSyncedAt(String id, Date syncedAt) {
        return jooqService.getConnection()
            .flatMap(connection -> {
                connection.update(TABLE)
                    .set(field("syncedAt"), syncedAt)
                    .where(field("id").eq(id))
                    .execute();
                return getTaskEntry(id);
            });
    }

    private Observable<TaskEntry> toTaskEntry(Record record) {
        Observable<Date> oCreatedAt = DateUtil.parse(record.getValue("createdAt").toString());
        Observable<Date> oUpdatedAt = Observable.empty();
        Observable<Date> oSyncedAt = Observable.empty();
        if (record.getValue("updatedAt") != null)
            oUpdatedAt = DateUtil.parse(record.getValue("updatedAt").toString());
        if (record.getValue("syncedAt") != null)
            oSyncedAt = DateUtil.parse(record.getValue("syncedAt").toString());
        return Observable.zip(oCreatedAt, oUpdatedAt.defaultIfEmpty(null), oSyncedAt.defaultIfEmpty(null),
            (createdAt, updatedAt, syncedAt) ->
            new TaskEntry(
                record.getValue("id").toString(),
                createdAt,
                record.getValue("taskId").toString(),
                record.getValue("inputId").toString(),
                Optional.ofNullable(updatedAt),
                Optional.ofNullable(syncedAt)));
    }
}