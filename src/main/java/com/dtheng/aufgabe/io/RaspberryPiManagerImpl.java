package com.dtheng.aufgabe.io;

import com.dtheng.aufgabe.AufgabeContext;
import com.dtheng.aufgabe.input.InputManager;
import com.dtheng.aufgabe.input.dto.InputsRequest;
import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.Configuration;
import com.dtheng.aufgabe.device.DeviceManager;
import com.dtheng.aufgabe.io.util.AufgabePinListenerDigital;
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

    private InputManager inputManager;
    private AufgabeContext aufgabeContext;
    private DeviceManager deviceManager;
    private ConfigManager configManager;

    @Inject
    public RaspberryPiManagerImpl(InputManager inputManager, AufgabeContext aufgabeContext, DeviceManager deviceManager, ConfigManager configManager) {
        this.inputManager = inputManager;
        this.aufgabeContext = aufgabeContext;
        this.deviceManager = deviceManager;
        this.configManager = configManager;
    }

    @Override
    public Observable<Void> startUp() {
        return Observable.zip(
            deviceManager.getDeviceId(),
            configManager.getConfig()
                .map(Configuration::getDeviceType),
            (deviceId, deviceType) -> {
                switch (deviceType) {
                    case RASPBERRY_PI:
                        controller = GpioFactory.getInstance();
                        InputsRequest request = new InputsRequest();
                        request.setOffset(0);
                        request.setLimit(10);
                        request.setDevice(Optional.of(deviceId));
                        return inputManager.get(request)
                            .flatMap(resp -> Observable.from(resp.getInputs())
                                .filter(input -> !digitalInputMap.containsKey(input.getIoPin()))
                                .flatMap(this::provisionDigitalInputPin)
                                .doOnNext(input -> {
                                    GpioPinDigitalInput digitalInput = digitalInputMap.get(input.getIoPin());
                                    AufgabePinListenerDigital listener = aufgabeContext.getInjector().getInstance(AufgabePinListenerDigital.class);
                                    listener.setInputId(input.getId());
                                    digitalInput.addListener(listener);
                                })
                                .toList());
                    default:
                        return Observable.empty();
                }
            })
            .flatMap(o -> o)
            .ignoreElements().cast(Void.class);
    }

    private Observable<Input> provisionDigitalInputPin(Input input) {
        switch (input.getIoPin()) {
            case "GPIO_00":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_00));
                break;
            case "GPIO_01":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_01));
                break;
            case "GPIO_02":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_02));
                break;
            case "GPIO_03":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_03));
                break;
            case "GPIO_04":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_04));
                break;
            case "GPIO_05":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_05));
                break;
            case "GPIO_06":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_06));
                break;
            case "GPIO_07":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_07));
                break;
            case "GPIO_08":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_08));
                break;
            case "GPIO_09":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_09));
                break;
            case "GPIO_10":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_10));
                break;
            case "GPIO_11":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_11));
                break;
            case "GPIO_12":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_12));
                break;
            case "GPIO_13":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_13));
                break;
            case "GPIO_14":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_14));
                break;
            case "GPIO_15":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_15));
                break;
            case "GPIO_16":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_16));
                break;
            case "GPIO_17":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_17));
                break;
            case "GPIO_18":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_18));
                break;
            case "GPIO_19":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_19));
                break;
            case "GPIO_20":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_20));
                break;
            case "GPIO_21":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_21));
                break;
            case "GPIO_22":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_22));
                break;
            case "GPIO_23":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_23));
                break;
            case "GPIO_24":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_24));
                break;
            case "GPIO_25":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_25));
                break;
            case "GPIO_26":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_26));
                break;
            case "GPIO_27":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_27));
                break;
            case "GPIO_28":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_28));
                break;
            case "GPIO_29":
                digitalInputMap.put(input.getIoPin(), controller.provisionDigitalInputPin(RaspiPin.GPIO_29));
                break;
            default:
                return Observable.error(new RuntimeException("Invalid ioPin: "+ input.getIoPin()));
        }
        return Observable.just(input);
    }
}