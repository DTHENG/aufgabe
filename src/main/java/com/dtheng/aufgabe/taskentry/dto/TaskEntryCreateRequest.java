package com.dtheng.aufgabe.taskentry.dto;

import lombok.*;

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
}
