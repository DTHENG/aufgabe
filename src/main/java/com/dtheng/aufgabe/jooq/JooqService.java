package com.dtheng.aufgabe.jooq;

import com.google.inject.ImplementedBy;
import org.jooq.DSLContext;
import rx.Observable;

import java.util.Map;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(JooqServiceImpl.class)
public interface JooqService {

    Observable<Map<String, Object>> startUp();

	Observable<DSLContext> getConnection();

	Observable<DSLContext> reconnect();
}
