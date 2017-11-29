package com.dtheng.aufgabe.task.dto;

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
public class TaskCreateRequest {

	private String description;

	private Optional<String> id = Optional.empty();
	private Optional<Date> createdAt = Optional.empty();
}
