package com.dtheng.aufgabe.io.util;

import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import rx.Observable;

public abstract class BaseGpioPinListenerDigital implements GpioPinListenerDigital {

	public abstract Observable<Void> startUp();
}
