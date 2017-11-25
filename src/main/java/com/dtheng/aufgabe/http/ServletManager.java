package com.dtheng.aufgabe.http;

import com.google.inject.ImplementedBy;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import rx.Observable;

import javax.servlet.Servlet;
import java.util.Map;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(ServletManagerImpl.class)
public interface ServletManager {

    Observable<Void> start(Integer port, Map<String, Class<? extends Servlet>> config);

    String fuck();
}
