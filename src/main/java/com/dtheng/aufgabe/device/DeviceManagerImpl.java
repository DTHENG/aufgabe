package com.dtheng.aufgabe.device;

import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.device.dto.*;
import com.dtheng.aufgabe.device.event.DeviceCreatedEvent;
import com.dtheng.aufgabe.device.event.DeviceUpdatedEvent;
import com.dtheng.aufgabe.device.model.Device;
import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.exceptions.UnsupportedException;
import com.dtheng.aufgabe.security.SecurityManager;
import com.dtheng.aufgabe.sync.SyncManager;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.Date;
import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class DeviceManagerImpl implements DeviceManager {

    private DeviceDAO deviceDAO;
    private DeviceService deviceService;
    private ConfigManager configManager;
    private EventManager eventManager;
    private SecurityManager securityManager;
    private SyncManager syncManager;

    @Inject
    public DeviceManagerImpl(DeviceDAO deviceDAO, DeviceService deviceService, ConfigManager configManager, EventManager eventManager, SecurityManager securityManager, SyncManager syncManager) {
        this.deviceDAO = deviceDAO;
        this.deviceService = deviceService;
        this.configManager = configManager;
        this.eventManager = eventManager;
        this.securityManager = securityManager;
        this.syncManager = syncManager;
    }

    @Override
    public Observable<Device> get(String id) {
        return deviceDAO.getDevice(id);
    }

    @Override
    public Observable<DevicesResponse> get(DevicesRequest request) {
        return Observable.zip(
            deviceService.getDeviceId(),
            deviceDAO.getDevices(request),
            (deviceId, devicesResponse) ->
                Observable.from(devicesResponse.getDevices())
                    .map(aggregateDevice -> {
                        if (aggregateDevice.getDevice().getId().equals(deviceId))
                            aggregateDevice.setSelf(true);
                        return aggregateDevice;
                    })
                    .toList()
                    .map(aggregateDevices -> {
                        devicesResponse.setDevices(aggregateDevices);
                        return devicesResponse;
                    }))
            .flatMap(o -> o);
    }

    @Override
    public Observable<Device> create(DeviceCreateRequest request) {
        return deviceService.getDeviceId()
            .flatMap(deviceId -> deviceDAO.createDevice(
                new Device(
                    request.getId().orElseGet(() -> deviceId),
                    request.getCreatedAt().orElseGet(Date::new),
                    request.getName(),
                    request.getDescription(),
                    Optional.empty(),
                    Optional.empty()))
                .doOnNext(device -> eventManager.getDeviceCreated().trigger(new DeviceCreatedEvent(device.getId()))));
    }

    @Override
    public Observable<Device> performSync(Device device) {
        DeviceSyncRequest request = new DeviceSyncRequest(
            device.getId(),
            device.getCreatedAt().getTime(),
            device.getName().orElseGet(() -> null),
            device.getDescription().orElseGet(() -> null),
            device.getSyncedAt().isPresent() ? device.getSyncedAt().get().getTime() : null);
        return Observable.zip(
            configManager.getConfig()
                .map(AufgabeConfig::getPublicKey),
            securityManager.getSignature(request),
            syncManager.getSyncClient(),
            (publicKey, signature, syncClient) ->
                syncClient.syncDevice(publicKey, signature, request))
            .flatMap(o -> o)
            .defaultIfEmpty(null)
            .flatMap(Void -> deviceDAO.setSyncedAt(device.getId(), new Date()));
    }

    @Override
    public Observable<Device> update(String id, DeviceUpdateRequest request) {
        return deviceDAO.update(id, request)
            .defaultIfEmpty(null)
            .flatMap(Void -> deviceDAO.setUpdatedAt(id, new Date()))
            .doOnNext(device -> eventManager.getDeviceUpdated().trigger(new DeviceUpdatedEvent(device.getId())));
    }
}
