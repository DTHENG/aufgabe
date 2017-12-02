package com.dtheng.aufgabe.jooq;

import com.google.inject.ImplementedBy;
import org.jooq.DSLContext;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(JooqServiceImpl.class)
public interface JooqService {

	Observable<DSLContext> getConnection();

	Observable<DSLContext> reconnect();
}