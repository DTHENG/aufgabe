package com.dtheng.aufgabe.input.handlers;

import com.dtheng.aufgabe.input.InputHandler;
import com.dtheng.aufgabe.input.model.Input;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@NoArgsConstructor
public class B00TMD43BS_SoilMoistureSensorInputHandler implements InputHandler {

    @Override
    public Observable<Void> startUp(Input input) {
        throw new RuntimeException("Unimplemented");
    }

    @Override
    public String getName() {
        return "Soil Moisture Sensor";
    }
}
