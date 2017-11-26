package com.dtheng.aufgabe.db.dao;

import com.dtheng.aufgabe.db.JooqManager;
import com.dtheng.aufgabe.db.dto.EntriesRequest;
import com.dtheng.aufgabe.db.dto.EntriesResponse;
import com.dtheng.aufgabe.db.model.TaskEntry;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SortOrder;
import org.jooq.Table;
import rx.Observable;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.jooq.impl.DSL.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class AufgabeDAO {

	private static final Table<Record> TASK_ENTRY = table("task_entry");

	private JooqManager jooqManager;

	@Inject
	public AufgabeDAO(JooqManager jooqManager) {
		this.jooqManager = jooqManager;
	}

	public Observable<TaskEntry> createTaskEntry(TaskEntry entry) {
		return jooqManager.getConnection()
				.doOnNext(connection -> connection.insertInto(TASK_ENTRY)
							.set(field("id"), entry.getId())
							.set(field("description"), entry.getDescription())
							.execute())
				.flatMap(Void -> getTaskEntry(entry.getId()));
	}

	public Observable<TaskEntry> getTaskEntry(String id) {
		return jooqManager.getConnection()
				.flatMap(connection -> Observable.from(connection.select()
					.from(TASK_ENTRY)
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

	public Observable<EntriesResponse> getEntries(EntriesRequest request) {
		return jooqManager.getConnection()
			.flatMap(connection -> {
				int total = connection.selectCount()
					.from(TASK_ENTRY)
					.fetchOne(0, int.class);
				return Observable.from(connection
					.select()
					.from(TASK_ENTRY)
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
					record.getValue("description").toString()));
		} catch (Throwable throwable) {
			return Observable.error(throwable);
		}
	}
}
