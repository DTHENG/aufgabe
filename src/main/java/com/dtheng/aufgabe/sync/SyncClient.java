package com.dtheng.aufgabe.sync;

import com.dtheng.aufgabe.device.dto.DeviceSyncRequest;
import com.dtheng.aufgabe.input.dto.InputSyncRequest;
import com.dtheng.aufgabe.task.dto.TaskSyncRequest;
import com.dtheng.aufgabe.taskentry.dto.TaskEntrySyncRequest;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.POST;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public interface SyncClient {

    @POST("/sync/task")
    Observable<Void> syncTask(@Header("Public-Key") String publicKey, @Header("Signature") String signature, @Body TaskSyncRequest request);

    @POST("/sync/entry")
    Observable<Void> syncTaskEntry(@Header("Public-Key") String publicKey, @Header("Signature") String signature, @Body TaskEntrySyncRequest request);

    @POST("/sync/input")
    Observable<Void> syncInput(@Header("Public-Key") String publicKey, @Header("Signature") String signature, @Body InputSyncRequest request);

    @POST("/sync/deviceId")
    Observable<Void> syncDevice(@Header("Public-Key") String publicKey, @Header("Signature") String signature, @Body DeviceSyncRequest request);
}
