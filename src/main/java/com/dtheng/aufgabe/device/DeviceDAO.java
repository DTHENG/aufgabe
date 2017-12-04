package com.dtheng.aufgabe.device;

import com.dtheng.aufgabe.device.dto.DevicesRequest;
import com.dtheng.aufgabe.device.dto.DevicesResponse;
import com.dtheng.aufgabe.device.exception.DeviceNotFoundException;
import com.dtheng.aufgabe.device.model.Device;
import com.dtheng.aufgabe.jooq.JooqService;
import com.dtheng.aufgabe.util.DateUtil;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Table;
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
public class DeviceDAO {

    private static final Table<Record> TABLE = table("input");

    private JooqService jooqService;

    @Inject
    public DeviceDAO(JooqService jooqService) {
        this.jooqService = jooqService;
    }

    Observable<Device> createDevice(Device device) {
        return jooqService.getConnection()
            .doOnNext(connection -> connection.insertInto(TABLE)
                .set(field("id"), device.getId())
                .set(field("createdAt"), device.getCreatedAt())
                .set(field("name"), device.getName().orElseGet(() -> null))
                .set(field("description"), device.getDescription().orElseGet(() -> null))
                .execute())
            .flatMap(Void -> getDevice(device.getId()));
    }

    Observable<Device> getDevice(String id) {
        return jooqService.getConnection()
            .flatMap(connection -> Observable.from(connection.select()
                .from(TABLE)
                .where(field("id").eq(id)
                    .and(field("removedAt").isNull()))
                .fetch()))
            .defaultIfEmpty(null)
            .map(record -> {
                if (record == null)
                    throw new DeviceNotFoundException(id);
                return record;
            })
            .flatMap(this::toDevice);
    }

    Observable<DevicesResponse> getDevices(DevicesRequest request) {
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
                    .offset(request.getOffset())
                    .limit(request.getLimit())
                    .fetch())
                    .concatMap(this::toDevice)
                    .toList()
                    .map(list -> new DevicesResponse(request.getOffset(), request.getLimit(), total, list));
            });
    }

    Observable<Device> setUpdatedAt(String id, Date updatedAt) {
        return jooqService.getConnection()
            .flatMap(connection -> {
                connection.update(TABLE)
                    .set(field("updatedAt"), updatedAt)
                    .where(field("id").eq(id))
                    .execute();
                return getDevice(id);
            });
    }

    Observable<Device> setSyncedAt(String id, Date syncedAt) {
        return jooqService.getConnection()
            .flatMap(connection -> {
                connection.update(TABLE)
                    .set(field("syncedAt"), syncedAt)
                    .where(field("id").eq(id))
                    .execute();
                return getDevice(id);
            });
    }

    private Observable<Device> toDevice(Record record) {
        Observable<Date> oCreatedAt = DateUtil.parse(record.getValue("createdAt").toString());
        Observable<Date> oUpdatedAt = Observable.empty();
        Observable<Date> oSyncedAt = Observable.empty();
        if (record.getValue("updatedAt") != null)
            oUpdatedAt = DateUtil.parse(record.getValue("updatedAt").toString());
        if (record.getValue("syncedAt") != null)
            oSyncedAt = DateUtil.parse(record.getValue("syncedAt").toString());
        return Observable.zip(
            oCreatedAt,
            oUpdatedAt.defaultIfEmpty(null),
            oSyncedAt.defaultIfEmpty(null),
            (createdAt, updatedAt, syncedAt) -> new Device(
                record.getValue("id").toString(),
                createdAt,
                Optional.ofNullable(record.getValue("name") != null ? record.getValue("name").toString() : null),
                Optional.ofNullable(record.getValue("description") != null ? record.getValue("description").toString() : null),
                Optional.ofNullable(updatedAt),
                Optional.ofNullable(syncedAt)));
    }
}