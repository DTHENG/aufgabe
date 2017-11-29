package com.dtheng.aufgabe.input.handlers;

import com.dtheng.aufgabe.AufgabeContext;
import com.dtheng.aufgabe.input.InputHandler;
import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.io.RaspberryPiManager;
import com.dtheng.aufgabe.io.util.B3F_TactileSwitchInputListener;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class B3F_TactileSwitchInputHandler implements InputHandler {

    private RaspberryPiManager raspberryPiManager;
    private AufgabeContext aufgabeContext;

    @Inject
    public B3F_TactileSwitchInputHandler(RaspberryPiManager raspberryPiManager, AufgabeContext aufgabeContext) {
        this.raspberryPiManager = raspberryPiManager;
        this.aufgabeContext = aufgabeContext;
    }

    @Override
    public Observable<Void> startUp(Input input) {
        return raspberryPiManager.getDigitalInput(input.getIoPin())
            .flatMap(digitalInput -> {
                B3F_TactileSwitchInputListener listener = aufgabeContext.getInjector().getInstance(B3F_TactileSwitchInputListener.class);
                listener.setInputId(input.getId());
                digitalInput.addListener(listener);
                return Observable.empty();
            });
    }
}
