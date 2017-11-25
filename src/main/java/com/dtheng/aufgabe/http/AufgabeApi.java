package com.dtheng.aufgabe.http;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class AufgabeApi {

    public static class NotFound extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
            res.setStatus(404);
            res.getWriter().write("{\"message\":\"Route not found\",\"code\":404}");
        }
    }

    public static class Entries extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
            res.setStatus(200);
            res.getWriter().write("[{\"hello\":\"world\"},{\"hello\":\"world\"},{\"hello\":\"world\"}]");
        }
    }
}