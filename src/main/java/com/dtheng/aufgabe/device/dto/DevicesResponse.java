package com.dtheng.aufgabe.device.dto;

import com.dtheng.aufgabe.device.model.Device;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DevicesResponse {

    private int offset = 0;
    private int limit = 10;
    private int total = 0;
    private List<Device> devices = new ArrayList<>();
}
