package com.dtheng.aufgabe.taskentry.dto;

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
public class TaskEntryCreateRequest {

	private String taskId;
    private String inputId;

    private Optional<String> id = Optional.empty();
    private Optional<Date> createdAt = Optional.empty();
}
