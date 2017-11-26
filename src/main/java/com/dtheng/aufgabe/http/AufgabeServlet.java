package com.dtheng.aufgabe.http;

import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.http.util.ErrorUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class AufgabeServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.error("Unable to find route for GET {}", req.getRequestURL().toString());
		ErrorUtil.handle(new AufgabeException("Route not found"), resp, 404)
				.subscribe(Void -> {},
						error -> log.error(error.toString()));
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.error("Unable to find route for POST {}", req.getRequestURL().toString());
		ErrorUtil.handle(new AufgabeException("Route not found"), resp, 404)
				.subscribe(Void -> {},
						error -> log.error(error.toString()));
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.error("Unable to find route for DELETE {}", req.getRequestURL().toString());
		ErrorUtil.handle(new AufgabeException("Route not found"), resp, 404)
				.subscribe(Void -> {},
						error -> log.error(error.toString()));
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.error("Unable to find route for OPTIONS {}", req.getRequestURL().toString());
		ErrorUtil.handle(new AufgabeException("Route not found"), resp, 404)
				.subscribe(Void -> {},
						error -> log.error(error.toString()));
	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.error("Unable to find route for HEAD {}", req.getRequestURL().toString());
		ErrorUtil.handle(new AufgabeException("Route not found"), resp, 404)
				.subscribe(Void -> {},
						error -> log.error(error.toString()));
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.error("Unable to find route for PUT {}", req.getRequestURL().toString());
		ErrorUtil.handle(new AufgabeException("Route not found"), resp, 404)
				.subscribe(Void -> {},
						error -> log.error(error.toString()));
	}

	@Override
	protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.error("Unable to find route for TRACE {}", req.getRequestURL().toString());
		ErrorUtil.handle(new AufgabeException("Route not found"), resp, 404)
				.subscribe(Void -> {},
						error -> log.error(error.toString()));
	}
}
