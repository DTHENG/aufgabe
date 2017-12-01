package com.dtheng.aufgabe.jooq;

import com.google.inject.ImplementedBy;
import org.jooq.DSLContext;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(JooqManagerImpl.class)
public interface JooqManager {

    Observable<Void> start();

	Observable<DSLContext> getConnection();

	Observable<DSLContext> reconnect();
}
