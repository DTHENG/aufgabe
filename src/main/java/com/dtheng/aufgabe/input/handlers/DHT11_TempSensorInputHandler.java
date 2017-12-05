package com.dtheng.aufgabe.input.handlers;

import com.dtheng.aufgabe.input.InputHandler;
import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.util.Temperature;
import com.pi4j.wiringpi.Gpio;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.concurrent.TimeUnit;

import static com.pi4j.wiringpi.Gpio.*;

/**
 * A lot of this code is based off this implementation:
 * https://github.com/marcandreuf/sunfounder-sensors-raspi-4j/blob/master/src/main/java/org/mandfer/sunfunpi4j/Ex20_DHT11.java
 *
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@NoArgsConstructor
public class DHT11_TempSensorInputHandler implements InputHandler {

    private Input input;

    @Override
    public Observable<Void> startUp(Input input) {

        this.input = input;

        if (Gpio.wiringPiSetup() == -1)
            return Observable.error(new RuntimeException("Gpio setup returned -1"));

        Observable.interval(1, TimeUnit.SECONDS)
            .flatMap(Void -> readData())
            .doOnNext(temperature -> log.info("Temperature: {}", temperature))
            .subscribe(Void -> {}, error -> {
                log.error(error.getMessage());
                error.printStackTrace();
            });

        return Observable.empty();
    }

    @Override
    public String getName() {
        return "Temperature and Humidity Sensor";
    }

    private Observable<Temperature> readData() {
        return Observable.defer(() -> {
            int pin = Integer.valueOf(input.getIoPin());

            log.debug("Attempting sensor read on pin #{}", pin);

            pinMode(pin, OUTPUT);

            log.debug("Pull pin down for 20 milliseconds");
            digitalWrite(pin, LOW);
            return Observable.timer(20, TimeUnit.MILLISECONDS)

                .doOnNext(Void -> log.debug("Pull pin up for 40 microseconds"))
                .doOnNext(Void -> digitalWrite(pin, HIGH))
                .delay(40, TimeUnit.MILLISECONDS)

                .doOnNext(Void -> log.debug("Prepare to read from the pin"))
                .doOnNext(Void -> pinMode(pin, INPUT))
                .delay(10, TimeUnit.MILLISECONDS)

                .flatMap(Void -> {
                    int[] sensorData = {0,0,0,0,0};
                    int maxTimings = 85;
                    int counter = 0;
                    int lastState = HIGH;
                    int j = 0;

                    log.debug("Detect change and read data");
                    for (int i = 0; i < maxTimings; i++){
                        while (digitalRead(pin) == lastState){
                            counter++;
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException ie) {
                                return Observable.error(ie);
                            }
                            if (counter == 255)
                                break;
                        }
                        lastState = digitalRead(pin);

                        if (counter == 255) {
                            log.debug("Counted 255 break 2");
                            break;
                        }

                        log.debug("Ignore first 3 transitions");
                        if (i >= 4 && i % 2 == 0) {
                            log.debug("Shove each bit into the storage bytes, counter: {}", counter);
                            sensorData[j / 8] <<= 1;
                            if (counter > 16)
                                sensorData[j / 8] |= 1;
                            j++;
                        } else {
                            log.debug("Ignore transition");
                        }
                    }

                    // check we read 40 bits (8bit x 5 ) + verify checksum in the last byte
                    // print it out if data is good
                    log.debug("j: {}", j);
                    if (j >= 40 && sensorData[4] == ((sensorData[0] + sensorData[1] + sensorData[2] + sensorData[3]) & 0xFF)) {
                        return Observable.just(new Temperature(
                            Double.valueOf(sensorData[0]+"."+sensorData[1]),
                            Double.valueOf(sensorData[2]+"."+sensorData[3]),
                            (double) (sensorData[2] * 9f / 5f + 32f)));
                    } else {
                        log.debug("Data not good, skip");
                        return Observable.empty();
                    }
                });
        });
    }
}
