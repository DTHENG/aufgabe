package com.dtheng.aufgabe.io;

import com.google.inject.*;
import com.pi4j.io.gpio.*;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@Singleton
public class RaspberryPiManagerImpl implements RaspberryPiManager {

    private GpioController controller = null;

    private Map<String, GpioPinDigitalInput> digitalInputMap = new HashMap<>();

    @Override
    public Observable<GpioPinDigitalInput> getDigitalInput(String ioPin) {
        if (digitalInputMap.containsKey(ioPin))
            return Observable.just(digitalInputMap.get(ioPin));
        switch (ioPin) {
            case "GPIO_00":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_00));
                break;
            case "GPIO_01":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_01));
                break;
            case "GPIO_02":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_02));
                break;
            case "GPIO_03":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_03));
                break;
            case "GPIO_04":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_04));
                break;
            case "GPIO_05":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_05));
                break;
            case "GPIO_06":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_06));
                break;
            case "GPIO_07":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_07));
                break;
            case "GPIO_08":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_08));
                break;
            case "GPIO_09":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_09));
                break;
            case "GPIO_10":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_10));
                break;
            case "GPIO_11":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_11));
                break;
            case "GPIO_12":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_12));
                break;
            case "GPIO_13":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_13));
                break;
            case "GPIO_14":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_14));
                break;
            case "GPIO_15":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_15));
                break;
            case "GPIO_16":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_16));
                break;
            case "GPIO_17":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_17));
                break;
            case "GPIO_18":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_18));
                break;
            case "GPIO_19":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_19));
                break;
            case "GPIO_20":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_20));
                break;
            case "GPIO_21":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_21));
                break;
            case "GPIO_22":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_22));
                break;
            case "GPIO_23":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_23));
                break;
            case "GPIO_24":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_24));
                break;
            case "GPIO_25":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_25));
                break;
            case "GPIO_26":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_26));
                break;
            case "GPIO_27":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_27));
                break;
            case "GPIO_28":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_28));
                break;
            case "GPIO_29":
                digitalInputMap.put(ioPin, controller.provisionDigitalInputPin(RaspiPin.GPIO_29));
                break;
            default:
                return Observable.error(new RuntimeException("Invalid ioPin: "+ ioPin));
        }
        return Observable.just(digitalInputMap.get(ioPin));
    }
}