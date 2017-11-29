package com.dtheng.aufgabe.input.dto;

import lombok.*;

import java.util.Date;
import java.util.Optional;

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
    private String taskId;
    private String handler;
    private Optional<Date> createdAt = Optional.empty();
}
