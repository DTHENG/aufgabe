package com.dtheng.aufgabe.task.dto;

import com.dtheng.aufgabe.input.model.Input;
import com.dtheng.aufgabe.task.model.Task;
import com.dtheng.aufgabe.taskentry.model.TaskEntry;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
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
public class AggregateTask {

    @JsonUnwrapped private Task task;
    private List<TaskEntry> entries;
    private List<Input> inputs;
}
