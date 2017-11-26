package com.dtheng.aufgabe.button;

import com.dtheng.aufgabe.button.dto.*;
import com.dtheng.aufgabe.button.model.Button;
import com.google.inject.ImplementedBy;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(ButtonManagerImpl.class)
public interface ButtonManager {

    Observable<Button> get(String id);

    Observable<ButtonsResponse> get(ButtonsRequest request);

    Observable<Button> create(ButtonCreateRequest request);

    Observable<Button> remove(String id);
}
