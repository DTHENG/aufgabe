package com.dtheng.aufgabe.device;

import com.google.inject.Singleton;
import com.pi4j.system.SystemInfo;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@Singleton
public class DeviceManagerImpl implements DeviceManager {

    private Optional<String> id = Optional.empty();

    @Override
    public Observable<String> getDeviceId() {
        if ( ! id.isPresent())
            return loadId();
        return Observable.just(id.get());
    }

    private Observable<String> loadId() {
        return getInetAddress()
            .flatMap(this::getMacAddressOrSerialNumber)
            .doOnNext(id -> this.id = Optional.of(id));
    }

    private Observable<InetAddress> getInetAddress() {
        try {
            return Observable.just(InetAddress.getLocalHost());
        } catch (UnknownHostException ue) {
            return Observable.error(ue);
        }
    }

    private Observable<String> getMacAddressOrSerialNumber(InetAddress ip) {
        return getNetwork(ip)
            .flatMap(this::getMacAddress)
            .flatMap(macAddress -> {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < macAddress.length; i++) {
                    sb.append(String.format("%02X%s", macAddress[i], (i < macAddress.length - 1) ? "-" : ""));
                }
                return Observable.just(sb.toString());
            })
            .onErrorResumeNext(throwable -> getSerialNumber());
    }

    private Observable<NetworkInterface> getNetwork(InetAddress ip) {
        try {
            return Observable.just(NetworkInterface.getByInetAddress(ip));
        } catch (SocketException se) {
            return Observable.error(se);
        }
    }

    private Observable<byte[]> getMacAddress(NetworkInterface networkInterface) {
        try {
            return Observable.just(networkInterface.getHardwareAddress());
        } catch (SocketException se) {
            return Observable.error(se);
        }
    }

    private Observable<String> getSerialNumber() {
        try {
            return Observable.just(SystemInfo.getSerial());
        } catch (Exception e) {
            return Observable.error(e);
        }
    }
}