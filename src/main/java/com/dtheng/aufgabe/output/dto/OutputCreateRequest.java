package com.dtheng.aufgabe.output.dto;

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
public class OutputCreateRequest {

    private String taskId;
    private String handler;
}
