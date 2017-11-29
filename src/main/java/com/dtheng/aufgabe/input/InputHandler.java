package com.dtheng.aufgabe.input;

import com.dtheng.aufgabe.input.model.Input;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public interface InputHandler {

    Observable<Void> startUp(Input input);
}
