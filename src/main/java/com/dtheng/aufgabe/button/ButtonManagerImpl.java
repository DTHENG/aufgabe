package com.dtheng.aufgabe.button;

import com.dtheng.aufgabe.button.dto.*;
import com.dtheng.aufgabe.button.model.Button;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.Configuration;
import com.dtheng.aufgabe.config.model.DeviceType;
import com.dtheng.aufgabe.device.DeviceManager;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.exceptions.UnsupportedException;
import com.dtheng.aufgabe.util.RandomString;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.Date;
import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class ButtonManagerImpl implements ButtonManager {

    private ButtonDAO buttonDAO;
    private DeviceManager deviceManager;
    private ConfigManager configManager;

    @Inject
    public ButtonManagerImpl(ButtonDAO buttonDAO, DeviceManager deviceManager, ConfigManager configManager) {
        this.buttonDAO = buttonDAO;
        this.deviceManager = deviceManager;
        this.configManager = configManager;
    }

    @Override
    public Observable<Button> get(String id) {
        return buttonDAO.getButton(id);
    }

    @Override
    public Observable<Button> create(ButtonCreateRequest request) {
        return Observable.zip(
            deviceManager.getDeviceId(),
            configManager.getConfig().map(Configuration::getDeviceType),
            (deviceId, deviceType) -> {
                if (deviceType != DeviceType.RASPBERRY_PI)
                    throw new UnsupportedException();
                return checkIfIoPinIsFreeThenCreate(deviceId, request);
            })
            .flatMap(o -> o);
    }

    @Override
    public Observable<ButtonsResponse> get(ButtonsRequest request) {
        return buttonDAO.getButtons(request);
    }

    @Override
    public Observable<Button> remove(String id) {
        return configManager.getConfig()
            .map(Configuration::getDeviceType)
            .flatMap(deviceType -> {
                if (deviceType != DeviceType.RASPBERRY_PI)
                    return Observable.error(new UnsupportedException());
                return get(id);
            })
            .flatMap(button -> buttonDAO.removeButton(id)
                .doOnNext(Void -> button.setRemovedAt(Optional.of(new Date())))
                .map(Void -> button));
    }

    private Observable<Button> checkIfIoPinIsFreeThenCreate(String deviceId, ButtonCreateRequest request) {
        ButtonsRequest existingButtonRequest = new ButtonsRequest();
        existingButtonRequest.setDevice(Optional.of(deviceId));
        existingButtonRequest.setIoPin(Optional.of(request.getIoPin()));
        return buttonDAO.getButtons(existingButtonRequest)
            .map(ButtonsResponse::getTotal)
            .flatMap(numberOfExistingButtons -> {
                if (numberOfExistingButtons > 0)
                    return Observable.error(new AufgabeException("Button already exists for IO Pin "+ request.getIoPin()));
                Button button = new Button();
                button.setId("button-"+ new RandomString(8).nextString());
                button.setCreatedAt(new Date());
                button.setIoPin(request.getIoPin());
                button.setTaskId(request.getTaskId());
                button.setDevice(deviceId);
                return buttonDAO.createButton(button);
            });
    }
}