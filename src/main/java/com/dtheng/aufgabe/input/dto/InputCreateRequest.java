package com.dtheng.aufgabe.input.dto;

import lombok.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class InputCreateRequest {

    private String ioPin;
    private String  taskId;
}
