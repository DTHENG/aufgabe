package com.dtheng.aufgabe.device.dto;

import lombok.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DeviceSyncRequest {

    private String id;
    private long createdAt;
    private String name;
    private String description;
}
