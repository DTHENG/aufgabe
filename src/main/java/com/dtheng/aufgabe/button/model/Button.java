package com.dtheng.aufgabe.button.model;

import com.pi4j.io.gpio.RaspiPin;
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
public class Button {

    private String id;
    private Date createdAt;
    private String ioPin;
    private String  taskId;
    private Optional<Date> removedAt;
}
