package com.dtheng.aufgabe.device;

import com.dtheng.aufgabe.config.model.DeviceType;
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

    @Override
    public Observable<Void> startUp() {
        return loadId()
            .ignoreElements().cast(Void.class);
    }

    @Override
    public Observable<String> getDeviceId() {
        if (id == null)
            return loadId();
        return Observable.just(id);
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
}