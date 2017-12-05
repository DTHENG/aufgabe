package com.dtheng.aufgabe.output.dto;

import com.dtheng.aufgabe.output.model.Output;
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
public class OutputsResponse {

    private int offset;
    private int limit;
    private int total;
    private List<Output> outputs;
}
