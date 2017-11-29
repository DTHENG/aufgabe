package com.dtheng.aufgabe.input.handlers;

import com.dtheng.aufgabe.input.InputHandler;
import com.dtheng.aufgabe.input.model.Input;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class GP2Y0A21YK0F_IrDistanceSensorInputHandler implements InputHandler {

    @Override
    public Observable<Void> startUp(Input input) {
        throw new RuntimeException("Unimplemented");
    }

    @Override
    public Observable<Void> shutdown() {
        throw new RuntimeException("Unimplemented");
    }
}
