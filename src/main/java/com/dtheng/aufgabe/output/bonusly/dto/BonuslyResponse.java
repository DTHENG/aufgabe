package com.dtheng.aufgabe.output.bonusly.dto;

import lombok.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BonuslyResponse {

    private boolean success;
    private String message;
}
