package com.dtheng.aufgabe.button;

import com.dtheng.aufgabe.button.dto.*;
import com.dtheng.aufgabe.button.model.Button;
import com.dtheng.aufgabe.device.DeviceManager;
import com.dtheng.aufgabe.exceptions.AufgabeException;
import com.dtheng.aufgabe.io.RaspberryPiManager;
import com.dtheng.aufgabe.util.RandomString;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class ButtonManagerImpl implements ButtonManager {

    private ButtonDAO buttonDAO;
    private DeviceManager deviceManager;

    @Inject
    public ButtonManagerImpl(ButtonDAO buttonDAO, DeviceManager deviceManager) {
        this.buttonDAO = buttonDAO;
        this.deviceManager = deviceManager;
    }

    @Override
    public Observable<Button> get(String id) {
        return buttonDAO.getButton(id);
    }

    @Override
    public Observable<Button> create(ButtonCreateRequest request) {
        return deviceManager.getDeviceId()
                .flatMap(deviceId -> {
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
                });
    }

    @Override
    public Observable<ButtonsResponse> get(ButtonsRequest request) {
        return buttonDAO.getButtons(request);
    }

    @Override
    public Observable<Button> remove(String id) {
        return get(id)
                .flatMap(button -> buttonDAO.removeButton(id)
                    .doOnNext(Void -> button.setRemovedAt(Optional.of(new Date())))
                    .map(Void -> button));
    }
}