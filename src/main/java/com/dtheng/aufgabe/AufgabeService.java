package com.dtheng.aufgabe;

import rx.Observable;

import java.util.Map;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public interface AufgabeService {

    Observable<Map<String, Object>> startUp();

    long order();
}
