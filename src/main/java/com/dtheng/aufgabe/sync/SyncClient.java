package com.dtheng.aufgabe.sync;

import com.dtheng.aufgabe.task.dto.TaskSyncRequest;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public interface SyncClient {

    @POST("/sync/task")
    Observable<Void> syncTask(@Body TaskSyncRequest request);
}
