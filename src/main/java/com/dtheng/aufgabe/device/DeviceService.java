package com.dtheng.aufgabe.device;

import com.google.inject.ImplementedBy;
import rx.Observable;

import java.util.Map;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(DeviceServiceImpl.class)
public interface DeviceService {

    Observable<Map<String, Object>> startUp();

    Observable<String> getDeviceId();
}
