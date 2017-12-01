package com.dtheng.aufgabe.input;

import com.dtheng.aufgabe.input.dto.*;
import com.dtheng.aufgabe.input.model.Input;
import com.google.inject.ImplementedBy;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(InputManagerImpl.class)
public interface InputManager {

    Observable<Input> get(String id);

    Observable<InputsResponse> get(InputsRequest request);

    Observable<Input> create(InputCreateRequest request);

    Observable<Input> remove(String id);

    Observable<Input> performSync(Input input);

    Observable<String> getDevices();
}
