package com.dtheng.aufgabe.device.dto;

import lombok.*;

import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DeviceUpdateRequest {

    private Optional<String> name = Optional.empty();
    private Optional<String> description = Optional.empty();
}
