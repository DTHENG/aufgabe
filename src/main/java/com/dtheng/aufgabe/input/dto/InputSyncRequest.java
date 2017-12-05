package com.dtheng.aufgabe.input.dto;

import com.dtheng.aufgabe.input.InputHandler;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InputSyncRequest {

    private String id;
    private long createdAt;
    private String ioPin;
    private String  taskId;
    private String deviceId;
    private String handler;
}
