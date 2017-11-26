package com.dtheng.aufgabe.button.dto;

import com.pi4j.io.gpio.RaspiPin;
import lombok.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ButtonCreateRequest {

    private String ioPin;
    private String  taskId;
}
