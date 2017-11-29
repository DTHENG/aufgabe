package com.dtheng.aufgabe.input.dto;

import lombok.*;

import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class InputsRequest {

    private int offset = 0;
    private int limit = 10;

    private Optional<String> taskId = Optional.empty();
    private Optional<String> device = Optional.empty();
    private Optional<String> ioPin = Optional.empty();
    private Optional<String> orderBy = Optional.empty();
    private Optional<String> orderDirection = Optional.empty();
}
