package com.dtheng.aufgabe.bonusly;

import com.dtheng.aufgabe.bonusly.dto.BonuslyRequest;
import com.dtheng.aufgabe.bonusly.dto.BonuslyResponse;
import retrofit.http.*;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public interface BonuslyApi {

    @POST("/bonuses")
    Observable<BonuslyResponse> create(@Header("Authorization") String authHeader, @Body BonuslyRequest request);

    @GET("/teapot")
    Observable<BonuslyResponse> test();
}
