package com.dtheng.aufgabe.http;

import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.security.SecurityManager;
import com.dtheng.aufgabe.security.exception.MissingPublicKey;
import com.dtheng.aufgabe.security.exception.MissingSignature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class HttpManagerImpl implements HttpManager {

    private SecurityManager securityManager;

    @Inject
    public HttpManagerImpl(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @Override
    public <T> Observable<T> getBody(HttpServletRequest request, Class<T> classRef) {
        return verifyRequest(request)
            .flatMap(Void -> getBodyFromRequest(request)
                .flatMap(rawBody -> securityManager.verifyRequest(request.getHeader("Public-Key"), request.getHeader("Signature"), rawBody)
                    .flatMap(verified -> parseBody(rawBody, classRef))));
    }

    @Override
    public Observable<Boolean> verifyGetRequest(HttpServletRequest request) {
        return verifyRequest(request)
            .flatMap(Void -> securityManager.verifyRequest(request.getHeader("Public-Key"), request.getHeader("Signature"), ""));
    }

    private Observable<String> getBodyFromRequest(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        Throwable throwable = null;
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException e) {
            throwable = e;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    throwable = e;
                }
            }
        }
        if (throwable != null)
            return Observable.error(throwable);
        return Observable.just(stringBuilder.toString());
    }

    private Observable<Boolean> verifyRequest(HttpServletRequest request) {
        if (request.getContentType() == null || ! request.getContentType().contains("application/json"))
            return Observable.error(new AufgabeException("Content-Type must be application/json"));
        Optional<String> publicKey = Optional.ofNullable(request.getHeader("Public-Key"));
        Optional<String> signature = Optional.ofNullable(request.getHeader("Signature"));
        if ( ! publicKey.isPresent())
            return Observable.error(new MissingPublicKey());
        if ( ! signature.isPresent())
            return Observable.error(new MissingSignature());
        return Observable.just(true);
    }

    private <T> Observable<T> parseBody(String body, Class<T> classRef) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        if (body.isEmpty())
            return Observable.empty();
        try {
            return Observable.just(objectMapper.readValue(body, classRef));
        } catch (IOException ioe) {
            return Observable.error(ioe);
        }
    }
}
