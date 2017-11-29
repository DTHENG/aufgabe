package com.dtheng.aufgabe.sync;

import com.dtheng.aufgabe.input.dto.InputSyncRequest;
import com.dtheng.aufgabe.task.dto.TaskSyncRequest;
import com.dtheng.aufgabe.taskentry.dto.TaskEntrySyncRequest;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public interface SyncClient {

    @POST("/sync/task")
    Observable<Void> syncTask(@Body TaskSyncRequest request);

    @POST("/sync/entry")
    Observable<Void> syncTaskEntry(@Body TaskEntrySyncRequest request);

    @POST("/sync/input")
    Observable<Void> syncInput(@Body InputSyncRequest request);
}
