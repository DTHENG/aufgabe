package com.dtheng.aufgabe.device;

import com.dtheng.aufgabe.device.model.DeviceType;
import com.google.inject.Singleton;
import com.pi4j.system.SystemInfo;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@Singleton
public class DeviceManagerImpl implements DeviceManager {

    private String id;
    private DeviceType type;

    @Override
    public Observable<Void> startUp() {
        log.info("Starting device manager...");
        return loadId()
                .flatMap(Void -> loadType())
                .doOnNext(Void -> log.info("Device manager setup! id: {}, type: {}", this.id, this.type))
                .ignoreElements().cast(Void.class);
    }

    @Override
    public Observable<String> getDeviceId() {
        if (id == null)
            return loadId();
        return Observable.just(id);
    }

    @Override
    public Observable<DeviceType> getDeviceType() {
        if (type == null)
            return loadType();
        return Observable.just(type);
    }

    private Observable<String> loadId() {
        return Observable.defer(() -> {
            try {
                InetAddress ip = InetAddress.getLocalHost();
                try {
                    NetworkInterface network = NetworkInterface.getByInetAddress(ip);
                    byte[] mac = network.getHardwareAddress();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    return Observable.just(sb.toString());
                } catch (NullPointerException npe) {
                    return Observable.just(SystemInfo.getSerial());
                }
            } catch (Throwable throwable) {
                return Observable.error(throwable);
            }
        })
        .doOnNext(id -> this.id = id);
    }

    private Observable<DeviceType> loadType() {
        return Observable.defer(() -> {
            try {
                InetAddress ip = InetAddress.getLocalHost();
                try {
                    NetworkInterface network = NetworkInterface.getByInetAddress(ip);
                    network.getHardwareAddress();
                    return Observable.just(DeviceType.MAC_OS);
                } catch (NullPointerException npe) {
                    return Observable.just(DeviceType.RASPBERRY_PI);
                }
            } catch (Throwable throwable) {
                return Observable.error(throwable);
            }
        })
        .doOnNext(type -> this.type = type);
    }

}
