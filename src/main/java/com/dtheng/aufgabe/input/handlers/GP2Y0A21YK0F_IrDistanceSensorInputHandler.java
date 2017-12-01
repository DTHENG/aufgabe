package com.dtheng.aufgabe.input.handlers;

import com.dtheng.aufgabe.AufgabeContext;
import com.dtheng.aufgabe.input.InputHandler;
import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.io.RaspberryPiManager;
import com.dtheng.aufgabe.io.util.GP2Y0A21YK0F_IrDistanceSensorInputListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@NoArgsConstructor
@Singleton
public class GP2Y0A21YK0F_IrDistanceSensorInputHandler implements InputHandler {

	private RaspberryPiManager raspberryPiManager;
	private AufgabeContext aufgabeContext;

	@Inject
	public GP2Y0A21YK0F_IrDistanceSensorInputHandler(RaspberryPiManager raspberryPiManager, AufgabeContext aufgabeContext) {
		this.raspberryPiManager = raspberryPiManager;
		this.aufgabeContext = aufgabeContext;
	}

	@Override
	public String getName() {
		return "IR Distance Sensor";
	}

	@Override
	public Observable<Void> startUp(Input input) {
		return raspberryPiManager.getDigitalInput(input.getIoPin())
			.doOnNext(digitalInput -> {
				GP2Y0A21YK0F_IrDistanceSensorInputListener listener = aufgabeContext.getInjector().getInstance(GP2Y0A21YK0F_IrDistanceSensorInputListener.class);
				listener.setInputId(input.getId());
				digitalInput.addListener(listener);
			})
			.ignoreElements().cast(Void.class);
	}
}
