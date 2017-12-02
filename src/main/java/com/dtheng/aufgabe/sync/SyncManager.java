package com.dtheng.aufgabe.sync;

import com.google.inject.ImplementedBy;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(SyncManagerImpl.class)
public interface SyncManager {

    Observable<SyncClient> getSyncClient();
}
