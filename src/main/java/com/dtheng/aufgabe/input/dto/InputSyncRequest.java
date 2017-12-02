package com.dtheng.aufgabe.input.dto;

import com.dtheng.aufgabe.input.InputHandler;
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
public class InputSyncRequest {

    private String id;
    private long createdAt;
    private String ioPin;
    private String  taskId;
    private String device;
    private String handler;
}
