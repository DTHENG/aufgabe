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
public class DevicesRequest {

    private int offset = 0;
    private int limit = 10;
    private boolean onlyShowNeedSync = false;
}
