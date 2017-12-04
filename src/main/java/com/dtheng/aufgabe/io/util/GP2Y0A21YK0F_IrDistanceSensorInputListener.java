package com.dtheng.aufgabe.io.util;

import com.dtheng.aufgabe.AufgabeService;
import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.taskentry.event.GP2Y0A21YK0F_IrDistanceSensorInputEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.Gpio;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@Singleton
public class GP2Y0A21YK0F_IrDistanceSensorInputListener implements GpioPinListenerDigital, AufgabeService {

    private static final int CRON_INTERVAL_MS = 1000 * 45;

    @Setter private String inputId;

    private static Date lastMovement = new Date();
    private static boolean isMotionless = false;

    private EventManager eventManager;

    @Inject
    public GP2Y0A21YK0F_IrDistanceSensorInputListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public Observable<Map<String, Object>> startUp() {
        Observable.interval(CRON_INTERVAL_MS, TimeUnit.MILLISECONDS)
            .flatMap(Void -> sensorCheckCron())
            .subscribe(Void -> {},
                error -> log.error(error.toString()));
        return Observable.empty();
    }

    @Override
    public long order() {
        return 1512352852;
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (Gpio.digitalRead(27) != 1)
            return;
        if (isMotionless)
            isMotionless = false;
        lastMovement = new Date();
    }

    private Observable<Void> sensorCheckCron() {
        return Observable.defer(() -> {
            if (inputId == null)
                return Observable.empty();
            long timeout = CRON_INTERVAL_MS * 3;
            long diff = new Date().getTime() - lastMovement.getTime();
            if (diff > timeout && ! isMotionless) {
				isMotionless = true;
				eventManager.getGP2Y0A21YK0F_IrDistanceSensorInput()
                    .trigger(new GP2Y0A21YK0F_IrDistanceSensorInputEvent(inputId, new Date(lastMovement.getTime())));
            }
            return Observable.empty();
        });
    }
}
