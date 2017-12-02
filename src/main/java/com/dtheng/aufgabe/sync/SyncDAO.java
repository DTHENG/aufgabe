package com.dtheng.aufgabe.sync;

import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.jooq.JooqService;
import com.dtheng.aufgabe.sync.model.SyncEntry;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.Table;
import rx.Observable;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class SyncDAO {

    private static final Table<Record> TABLE = table("sync_entry");

    private JooqService jooqService;

    @Inject
    public SyncDAO(JooqService jooqService) {
        this.jooqService = jooqService;
    }

    Observable<SyncEntry> createEntry(SyncEntry entry) {
        return jooqService.getConnection()
            .doOnNext(connection -> connection.insertInto(TABLE)
                .set(field("id"), entry.getId())
                .execute())
            .flatMap(Void -> getEntry(entry.getId()));
    }

    Observable<SyncEntry> getEntry(String id) {
        return jooqService.getConnection()
            .flatMap(connection -> Observable.from(connection.select()
                .from(TABLE)
                .where(field("id").eq(id))
                .fetch()))
            .defaultIfEmpty(null)
            .flatMap(record -> {
                if (record == null) {
                    log.error("Sync Entry not found, id: {}", id);
                    return Observable.error(new AufgabeException("Sync Entry not found"));
                }
                return Observable.just(record);
            })
            .flatMap(this::toEntry);
    }

    private Observable<SyncEntry> toEntry(Record record) {
        try {
            SyncEntry syncEntry = new SyncEntry();
            syncEntry.setId(record.getValue("id").toString());
            syncEntry.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(record.getValue("createdAt").toString()));
            if (record.getValue("startedAt") != null)
                syncEntry.setStartedAt(Optional.ofNullable(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(record.getValue("startedAt").toString())));
            if (record.getValue("completedAt") != null)
                syncEntry.setCompletedAt(Optional.ofNullable(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(record.getValue("completedAt").toString())));
            if (record.getValue("numberOfRecordsSynced") != null)
                syncEntry.setNumberOfRecordsSynced(Optional.ofNullable(Integer.valueOf(record.getValue("numberOfRecordsSynced").toString())));
            if (record.getValue("recordsSynced") != null)
                syncEntry.setRecordsSynced(Optional.of(Arrays.asList(record.getValue("recordsSynced").toString().split(","))));
            return Observable.just(syncEntry);
        } catch (Throwable throwable) {
            return Observable.error(throwable);
        }
    }
}
