package com.dtheng.aufgabe.button;

import com.dtheng.aufgabe.button.dto.*;
import com.dtheng.aufgabe.button.model.Button;
import com.dtheng.aufgabe.util.RandomString;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.Date;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class ButtonManagerImpl implements ButtonManager {

    private ButtonDAO buttonDAO;

    @Inject
    public ButtonManagerImpl(ButtonDAO buttonDAO) {
        this.buttonDAO = buttonDAO;
    }

    @Override
    public Observable<Button> get(String id) {
        return buttonDAO.getButton(id);
    }

    @Override
    public Observable<Button> create(ButtonCreateRequest request) {
        Button button = new Button();
        button.setId(new RandomString(8).nextString());
        button.setCreatedAt(new Date());
        button.setIoPin(request.getIoPin());
        button.setTaskId(request.getTaskId());
        return buttonDAO.createButton(button);
    }

    @Override
    public Observable<ButtonsResponse> get(ButtonsRequest request) {
        return buttonDAO.getButtons(request);
    }
}