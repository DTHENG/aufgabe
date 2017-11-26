package com.dtheng.aufgabe.http.util;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class ErrorUtil {

	public static Observable<Void> handle(Throwable throwable, HttpServletResponse resp) {
		return handle(throwable, resp, 500);
	}

	public static Observable<Void> handle(Throwable throwable, HttpServletResponse resp, int code) {
		log.error("Returning {} error: {}", code, throwable.getLocalizedMessage());
//		throwable.printStackTrace();
		resp.setStatus(code);
		resp.setHeader("Server", "Raspberry Pi 3");
		resp.setContentType("application/json");
		try {
			resp.getWriter().write("{\"message\":\""+ throwable.getLocalizedMessage() +"\",\"code\":"+ code +"}");
		} catch (Exception e) {
			return Observable.error(e);
		}
		return Observable.empty();
	}
}
