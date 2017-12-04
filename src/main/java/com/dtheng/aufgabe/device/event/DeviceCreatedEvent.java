package com.dtheng.aufgabe.device.event;

import lombok.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DeviceCreatedEvent {

    private String id;
}
