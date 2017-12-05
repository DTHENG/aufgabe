package com.dtheng.aufgabe.output;

import com.dtheng.aufgabe.output.model.Output;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public interface OutputHandler {

    Observable<Void> startUp(Output input);

    String getName();
}
