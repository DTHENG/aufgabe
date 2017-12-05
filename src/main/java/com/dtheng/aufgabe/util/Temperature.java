package com.dtheng.aufgabe.util;

import lombok.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Temperature {

    private double humidity;
    private double celsius;
    private double fahrenheit;
}
