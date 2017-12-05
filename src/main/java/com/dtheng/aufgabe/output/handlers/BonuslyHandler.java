package com.dtheng.aufgabe.output.handlers;

import com.dtheng.aufgabe.output.OutputHandler;
import com.dtheng.aufgabe.output.model.Output;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class BonuslyHandler implements OutputHandler {

    @Override
    public Observable<Void> startUp(Output input) {
        return Observable.empty();
    }

    @Override
    public String getName() {
        return "Bonusly Api";
    }
}
