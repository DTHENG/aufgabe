package com.dtheng.aufgabe.device;

import com.google.inject.ImplementedBy;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(DeviceServiceImpl.class)
public interface DeviceService {

    Observable<String> getDeviceId();
}
