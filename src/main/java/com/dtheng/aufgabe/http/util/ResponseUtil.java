package com.dtheng.aufgabe.http.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class ResponseUtil {

    public static Observable<Void> set(HttpServletResponse resp, Object body, int status) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            resp.setStatus(status);
            resp.setHeader("Server", "Raspberry Pi 3");
            resp.setContentType("application/json");
            try {
                resp.getWriter().write(writer.writeValueAsString(body)+"\n");
                return Observable.empty();
            } catch (JsonProcessingException jpe) {
                log.error("Error serializing json: {}", body.toString());
//				jpe.printStackTrace();
                log.error("Returning 500 with error");
                resp.setStatus(500);
                resp.getWriter().write("{\"message\":\"An error has occurred.\",\"code\":500}\n");
                return Observable.empty();
            }
        } catch (Throwable throwable) {
            return Observable.error(throwable);
        }
    }
}