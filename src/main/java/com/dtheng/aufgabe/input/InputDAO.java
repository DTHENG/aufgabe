package com.dtheng.aufgabe.input;

import com.dtheng.aufgabe.input.dto.*;
import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.jooq.JooqService;
import com.dtheng.aufgabe.util.DateUtil;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import rx.Observable;

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

    private JooqService jooqService;

    @Inject
    public InputDAO(JooqService jooqService) {
        this.jooqService = jooqService;
    }

    Observable<Input> createInput(Input input) {
        return jooqService.getConnection()
            .doOnNext(connection -> connection.insertInto(TABLE)
                .set(field("id"), input.getId())
                .set(field("createdAt"), input.getCreatedAt())
                .set(field("ioPin"), input.getIoPin())
                .set(field("taskId"), input.getTaskId())
                .set(field("device"), input.getDevice())
                .set(field("handler"), input.getHandler().getCanonicalName())
                .execute())
            .flatMap(Void -> getInput(input.getId()));
    }

    Observable<Input> getInput(String id) {
        return jooqService.getConnection()
            .flatMap(connection -> Observable.from(connection.select()
                .from(TABLE)
                .where(field("id").eq(id))
                .fetch()))
            .defaultIfEmpty(null)
            .map(record -> {
                if (record == null) {
                    log.error("Input not found, id: {}", id);
                    throw new AufgabeException("Input not found");
                }
                return record;
            })
            .flatMap(this::toInput);
    }

    Observable<Void> removeInput(String id) {
        return jooqService.getConnection()
            .flatMap(connection -> {
                connection.update(TABLE)
                    .set(field("removedAt"), new Date())
                    .where(field("id").eq(id))
                    .execute();
                return Observable.empty();
            });
    }

    Observable<InputsResponse> getInputs(InputsRequest request) {
        return jooqService.getConnection()
            .flatMap(connection -> {
                List<Condition> where = new ArrayList<>();
                where.add(field("removedAt").isNull());
                if (request.getTaskId().isPresent())
                    where.add(field("taskId").eq(request.getTaskId().get()));
                if (request.getDevice().isPresent())
                    where.add(field("device").eq(request.getDevice().get()));
                if (request.getIoPin().isPresent())
                    where.add((field("ioPin").eq(request.getIoPin().get())));
                if (request.getHandler().isPresent())
                    where.add(field("handler").eq(request.getHandler().get()));
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

    Observable<Input> setUpdatedAt(String id, Date updatedAt) {
        return jooqService.getConnection()
            .flatMap(connection -> {
                connection.update(TABLE)
                    .set(field("updatedAt"), updatedAt)
                    .where(field("id").eq(id))
                    .execute();
                return getInput(id);
            });
    }

    Observable<Input> setSyncedAt(String id, Date syncedAt) {
        return jooqService.getConnection()
            .flatMap(connection -> {
                connection.update(TABLE)
                    .set(field("syncedAt"), syncedAt)
                    .where(field("id").eq(id))
                    .execute();
                return getInput(id);
            });
    }

    Observable<String> getDevices() {
        return jooqService.getConnection()
            .flatMap(connection -> Observable.from(
                connection
                    .selectDistinct(field("device"))
                    .from(TABLE)
                    .fetch()))
            .map(result -> result.getValue("device").toString());
    }

    private Observable<Class<? extends InputHandler>> getHandler(String className) {
        return getClassForName(className)
            .flatMap(rawClass -> getNewInstanceOfClass(rawClass)
                .map(instance -> {
                    if ( ! (instance instanceof InputHandler))
                        throw new RuntimeException("Not an instance of InputHandler! "+ className);
                    return (Class<? extends InputHandler>) rawClass;
                }));
    }

    private Observable<Input> toInput(Record record) {
        Observable<Date> oCreatedAt = DateUtil.parse(record.getValue("createdAt").toString());
        Observable<Date> oRemovedAt = Observable.empty();
        Observable<Date> oUpdatedAt = Observable.empty();
        Observable<Date> oSyncedAt = Observable.empty();
        if (record.getValue("removedAt") != null)
            oRemovedAt = DateUtil.parse(record.getValue("removedAt").toString());
        if (record.getValue("updatedAt") != null)
            oUpdatedAt = DateUtil.parse(record.getValue("updatedAt").toString());
        if (record.getValue("syncedAt") != null)
            oSyncedAt = DateUtil.parse(record.getValue("syncedAt").toString());
        String className = record.getValue("handler").toString();
        return Observable.zip(
            oCreatedAt,
            oRemovedAt.defaultIfEmpty(null),
            oUpdatedAt.defaultIfEmpty(null),
            oSyncedAt.defaultIfEmpty(null),
            getHandler(className),
            (createdAt, removedAt, updatedAt, syncedAt, handler) -> new Input(
                record.getValue("id").toString(),
                createdAt,
                record.getValue("ioPin").toString(),
                record.getValue("taskId").toString(),
                record.getValue("device").toString(),
                Optional.ofNullable(removedAt),
                handler,
                Optional.ofNullable(updatedAt),
                Optional.ofNullable(syncedAt)));
    }

    private Observable<Class> getClassForName(String name) {
        try {
            return Observable.just(Class.forName(name));
        } catch (ClassNotFoundException cnfe) {
            return Observable.error(cnfe);
        }
    }

    private Observable<Object> getNewInstanceOfClass(Class clazz) {
        try {
            return Observable.just(clazz.newInstance());
        } catch (Exception e) {
            return Observable.error(e);
        }
    }
}