package com.dtheng.aufgabe.io.event;

import lombok.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ButtonPressedEvent {

    private String buttonId;
}