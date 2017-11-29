package com.dtheng.aufgabe.taskentry.model;

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
public class TaskEntry {

    private String id;
    private Date createdAt = new Date();
    private String taskId;
    private String inputId;
    private Optional<Date> updatedAt = Optional.empty();
    private Optional<Date> syncedAt = Optional.empty();
}
