package com.dtheng.aufgabe.device.dto;

import com.dtheng.aufgabe.device.model.Device;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AggregateDevice {

    @JsonUnwrapped private Device device;
    private boolean self = false;
}
