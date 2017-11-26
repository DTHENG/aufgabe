package com.dtheng.aufgabe.task.dto;

import lombok.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TasksRequest {

	private int offset;
	private int limit;
}
