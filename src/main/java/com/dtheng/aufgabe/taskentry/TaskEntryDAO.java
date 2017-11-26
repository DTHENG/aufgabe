package com.dtheng.aufgabe.taskentry;

import com.dtheng.aufgabe.jooq.JooqManager;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.taskentry.dto.EntriesRequest;
import com.dtheng.aufgabe.taskentry.dto.EntriesResponse;
import com.dtheng.aufgabe.taskentry.model.TaskEntry;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SortOrder;
import org.jooq.Table;
import rx.Observable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
class TaskEntryDAO {

    private static final Table<Record> TABLE = table("task_entry");

    private JooqManager jooqManager;

    @Inject
    public TaskEntryDAO(JooqManager jooqManager) {
        this.jooqManager = jooqManager;
    }

    Observable<TaskEntry> createTaskEntry(TaskEntry entry) {
        return jooqManager.getConnection()
            .doOnNext(connection -> connection.insertInto(TABLE)
                .set(field("id"), entry.getId())
                .set(field("taskId"), entry.getTaskId())
                .execute())
            .flatMap(Void -> getTaskEntry(entry.getId()));
    }

    Observable<TaskEntry> getTaskEntry(String id) {
        return jooqManager.getConnection()
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
        return jooqManager.getConnection()
            .flatMap(connection -> {
                List<Condition> where = new ArrayList<>();

                if (request.getTaskId().isPresent())
                    where.add(field("taskId").eq(request.getTaskId().get()));

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

    private Observable<TaskEntry> toTaskEntry(Record record) {
        try {
            return Observable.just(new TaskEntry(
                record.getValue("id").toString(),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(record.getValue("createdAt").toString()),
                record.getValue("taskId").toString()));
        } catch (Throwable throwable) {
            return Observable.error(throwable);
        }
    }
}