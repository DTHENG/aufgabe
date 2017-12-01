package com.dtheng.aufgabe.bonusly;

import com.dtheng.aufgabe.bonusly.dto.BonuslyResponse;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public interface BonuslyApi {

    @POST("/bonuses")
    Observable<BonuslyResponse> create(@Header("Authorization") String authHeader, @Path("reason") String message);
}
