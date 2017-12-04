package com.dtheng.aufgabe.http;

import com.google.inject.ImplementedBy;
import rx.Observable;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(HttpManagerImpl.class)
public interface HttpManager {

    <T> Observable<T> getBody(HttpServletRequest request, Class<T> classRef);

    Observable<Boolean> verifyGetRequest(HttpServletRequest request);
}
