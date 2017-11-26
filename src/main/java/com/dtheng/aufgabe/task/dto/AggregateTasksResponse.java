package com.dtheng.aufgabe.task.dto;

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
public class AggregateTasksResponse {

    private int offset;
    private int limit;
    private int total;
    private List<AggregateTask> tasks;
}
