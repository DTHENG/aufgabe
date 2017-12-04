package com.dtheng.aufgabe.security;

import com.google.inject.ImplementedBy;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(SecurityManagerImpl.class)
public interface SecurityManager {

    Observable<Boolean> verifyRequest(String publicKey, String signature, String request);

    Observable<String> getSignature(Object object);

    Observable<String> getSignature(String data);
}
