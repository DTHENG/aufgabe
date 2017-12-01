package com.dtheng.aufgabe.stats.dto;

import lombok.*;

import java.util.List;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AggregateDevice {

    private String id;
    private List<String> activeInputs;
}
