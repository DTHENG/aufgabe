package com.dtheng.aufgabe.device;

import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.device.dto.DeviceCreateRequest;
import com.dtheng.aufgabe.device.dto.DeviceSyncRequest;
import com.dtheng.aufgabe.device.dto.DevicesRequest;
import com.dtheng.aufgabe.device.dto.DevicesResponse;
import com.dtheng.aufgabe.device.event.DeviceCreatedEvent;
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
        return deviceDAO.getDevices(request);
    }

    @Override
    public Observable<Device> create(DeviceCreateRequest request) {
        return deviceService.getDeviceId()
            .flatMap(deviceId -> configManager.getConfig().map(AufgabeConfig::getDeviceType)
                .flatMap(deviceType -> {
                    switch (deviceType) {
                        case RASPBERRY_PI:
                        case MAC_OS:
                            return deviceDAO.createDevice(
                                new Device(
                                    request.getId(),
                                    request.getCreatedAt().orElseGet(() -> new Date()),
                                    request.getName(),
                                    request.getDescription(),
                                    Optional.empty(),
                                    Optional.empty()));
                        default:
                            return Observable.error(new UnsupportedException());
                    }
                })
                .doOnNext(device -> eventManager.getDeviceCreated().trigger(new DeviceCreatedEvent(device.getId()))));
    }

    @Override
    public Observable<Device> performSync(Device device) {
        DeviceSyncRequest request = new DeviceSyncRequest(device.getId(), device.getCreatedAt().getTime(), device.getName().orElseGet(() -> null), device.getDescription().orElseGet(() -> null));
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
}
