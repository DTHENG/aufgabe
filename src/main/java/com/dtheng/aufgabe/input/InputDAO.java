package com.dtheng.aufgabe.input;

import com.dtheng.aufgabe.input.dto.*;
import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.jooq.JooqManager;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
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
class InputDAO {

    private static final Table<Record> TABLE = table("input");

    private JooqManager jooqManager;

    @Inject
    public InputDAO(JooqManager jooqManager) {
        this.jooqManager = jooqManager;
    }

    Observable<Input> createInput(Input input) {
        return jooqManager.getConnection()
            .doOnNext(connection -> connection.insertInto(TABLE)
                .set(field("id"), input.getId())
                .set(field("ioPin"), input.getIoPin())
                .set(field("taskId"), input.getTaskId())
                .set(field("device"), input.getDevice())
                .execute())
            .flatMap(Void -> getInput(input.getId()));
    }

    Observable<Input> getInput(String id) {
        return jooqManager.getConnection()
            .flatMap(connection -> Observable.from(connection.select()
                .from(TABLE)
                .where(field("id").eq(id))
                .fetch()))
            .retryWhen(e -> e.flatMap(throwable -> {
                log.info("Got error looking up input {}, {}", id, throwable.toString());
                if (throwable instanceof DataAccessException)
                    return Observable.just(null);
                return Observable.error(throwable);
            }))
            .defaultIfEmpty(null)
            .flatMap(record -> {
                if (record == null) {
                    log.error("Input not found, id: {}", id);
                    return Observable.error(new AufgabeException("Input not found"));
                }
                return Observable.just(record);
            })
            .flatMap(this::toInput);
    }

    Observable<Void> removeInput(String id) {
        return jooqManager.getConnection()
            .flatMap(connection -> {
                connection.update(TABLE)
                    .set(field("removedAt"), new Date())
                    .where(field("id").eq(id))
                    .execute();
                return Observable.empty();
            });
    }

    Observable<InputsResponse> getInputs(InputsRequest request) {
        return jooqManager.getConnection()
            .flatMap(connection -> {

                List<Condition> where = new ArrayList<>();
                where.add(field("removedAt").isNull());
                if (request.getTaskId().isPresent())
                    where.add(field("taskId").eq(request.getTaskId().get()));
                if (request.getDevice().isPresent())
                    where.add(field("device").eq(request.getDevice().get()));
                if (request.getIoPin().isPresent())
                    where.add((field("ioPin").eq(request.getIoPin().get())));
                int total = connection.selectCount()
                    .from(TABLE)
                    .where(where)
                    .fetchOne(0, int.class);

                return Observable.from(connection
                    .select()
                    .from(TABLE)
                    .where(where)
                    .orderBy(
                        field(request.getOrderBy().isPresent() ? request.getOrderBy().get() : "createdAt")
                            .sort(request.getOrderDirection().isPresent() ? (request.getOrderDirection().get().toLowerCase().equals("asc") ? SortOrder.ASC : SortOrder.DESC) : SortOrder.DESC))
                    .offset(request.getOffset())
                    .limit(request.getLimit())
                    .fetch())
                    .concatMap(this::toInput)
                    .toList()
                    .map(list -> new InputsResponse(request.getOffset(), request.getLimit(), total, list));
            });
    }

    private Observable<Input> toInput(Record record) {
        try {
            return Observable.just(new Input(
                record.getValue("id").toString(),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(record.getValue("createdAt").toString()),
                record.getValue("ioPin").toString(),
                record.getValue("taskId").toString(),
                record.getValue("device").toString(),
                Optional.ofNullable(record.getValue("removedAt") == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(record.getValue("removedAt").toString()))));
        } catch (Throwable throwable) {
            return Observable.error(throwable);
        }
    }
}