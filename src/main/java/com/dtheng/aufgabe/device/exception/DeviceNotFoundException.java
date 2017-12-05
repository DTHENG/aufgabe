package com.dtheng.aufgabe.device.exception;

import com.dtheng.aufgabe.exceptions.AufgabeException;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class DeviceNotFoundException extends AufgabeException {

    public DeviceNotFoundException(String id) {
        super("Device not found, id: "+ id);
    }
}
