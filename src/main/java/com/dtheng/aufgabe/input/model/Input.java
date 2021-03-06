package com.dtheng.aufgabe.input.model;

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
public class Input {

    private String id;
    private Date createdAt;
    private String ioPin;
    private String  taskId;
    private String deviceId;
    private Optional<Date> removedAt = Optional.empty();
    private Class<? extends InputHandler> handler;
    private Optional<Date> updatedAt = Optional.empty();
    private Optional<Date> syncedAt = Optional.empty();
}
