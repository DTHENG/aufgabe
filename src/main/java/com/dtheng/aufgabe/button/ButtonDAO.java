package com.dtheng.aufgabe.button;

import com.dtheng.aufgabe.button.dto.*;
import com.dtheng.aufgabe.button.model.Button;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.jooq.JooqManager;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import rx.Observable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.jooq.impl.DSL.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class ButtonDAO {

    private static final Table<Record> TABLE = table("button");

    private JooqManager jooqManager;

    @Inject
    public ButtonDAO(JooqManager jooqManager) {
        this.jooqManager = jooqManager;
    }

    public Observable<Button> createButton(Button button) {
        return jooqManager.getConnection()
                .doOnNext(connection -> connection.insertInto(TABLE)
                        .set(field("id"), button.getId())
                        .set(field("ioPin"), button.getIoPin().toString())
                        .set(field("taskId"), button.getTaskId())
                        .execute())
                .flatMap(Void -> getButton(button.getId()));
    }

    public Observable<Button> getButton(String id) {
        return jooqManager.getConnection()
                .flatMap(connection -> Observable.from(connection.select()
                        .from(TABLE)
                        .where(field("id").eq(id))
                        .fetch()))
                .defaultIfEmpty(null)
                .flatMap(record -> {
                    if (record == null) {
                        log.error("Button not found, id: {}", id);
                        return Observable.error(new AufgabeException("Button not found"));
                    }
                    return Observable.just(record);
                })
                .flatMap(this::toButton);
    }

    public Observable<ButtonsResponse> getButtons(ButtonsRequest request) {
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
                            .concatMap(this::toButton)
                            .toList()
                            .map(list -> new ButtonsResponse(request.getOffset(), request.getLimit(), total, list));
                });
    }

    private Observable<Button> toButton(Record record) {
        try {
            return Observable.just(new Button(
                    record.getValue("id").toString(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(record.getValue("createdAt").toString()),
                    record.getValue("ioPin").toString(),
                    record.getValue("taskId").toString(),
                    Optional.ofNullable(record.getValue("removedAt") == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(record.getValue("removedAt").toString()))));
        } catch (Throwable throwable) {
            return Observable.error(throwable);
        }
    }
}