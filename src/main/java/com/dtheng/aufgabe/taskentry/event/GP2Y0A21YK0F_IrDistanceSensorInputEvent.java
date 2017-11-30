package com.dtheng.aufgabe.taskentry.event;

import lombok.*;

import java.util.Date;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GP2Y0A21YK0F_IrDistanceSensorInputEvent {

    private String inputId;
    private Date lastMovement;
}
