package com.dtheng.aufgabe.device;

import com.google.inject.ImplementedBy;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(DeviceManagerImpl.class)
public interface DeviceManager {

    Observable<Void> startUp();

    Observable<String> getDeviceId();
}
