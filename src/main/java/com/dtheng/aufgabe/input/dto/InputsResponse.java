package com.dtheng.aufgabe.input.dto;

import com.dtheng.aufgabe.input.model.Input;
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
public class InputsResponse {

    private int offset;
    private int limit;
    private int total;
    private List<Input> inputs;
}
