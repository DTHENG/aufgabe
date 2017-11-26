package com.dtheng.aufgabe.task.dto;

import com.dtheng.aufgabe.task.model.Task;
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
public class TasksResponse {

	private int offset;
	private int limit;
	private int total;
	private List<Task> tasks;
}
