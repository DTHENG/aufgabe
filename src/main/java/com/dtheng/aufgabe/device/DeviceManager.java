package com.dtheng.aufgabe.device;

import com.dtheng.aufgabe.device.dto.DeviceCreateRequest;
import com.dtheng.aufgabe.device.dto.DevicesRequest;
import com.dtheng.aufgabe.device.dto.DevicesResponse;
import com.dtheng.aufgabe.device.model.Device;
import com.google.inject.ImplementedBy;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(DeviceManagerImpl.class)
public interface DeviceManager {

    Observable<Device> get(String id);

    Observable<DevicesResponse> get(DevicesRequest request);

    Observable<Device> create(DeviceCreateRequest request);

    Observable<Device> performSync(Device device);
}
