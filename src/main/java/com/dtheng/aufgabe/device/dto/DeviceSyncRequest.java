package com.dtheng.aufgabe.device.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceSyncRequest {

    private String id;
    private long createdAt;
    private String name;
    private String description;
    private long syncedAt;
}
