package com.dtheng.aufgabe.io;

import com.google.inject.ImplementedBy;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(RaspberryPiManagerImpl.class)
public interface RaspberryPiManager {

    Observable<GpioPinDigitalInput> getDigitalInput(String ioPin);
}
