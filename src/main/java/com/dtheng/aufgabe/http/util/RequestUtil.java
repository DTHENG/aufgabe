package com.dtheng.aufgabe.http.util;

import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import rx.Observable;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class RequestUtil {

    public static <T> Observable<T> getBody(HttpServletRequest request, Class<T> classRef) {
        if (request.getContentType() == null || ! request.getContentType().contains("application/json"))
            return Observable.error(new AufgabeException("Content-Type must be application/json"));
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
        ObjectMapper objectMapper = new ObjectMapper();
        if (stringBuilder.toString().isEmpty())
            return Observable.empty();
        try {
            return Observable.just(objectMapper.readValue(stringBuilder.toString(), classRef));
        } catch (IOException ioe) {
            return Observable.error(ioe);
        }
    }
}
