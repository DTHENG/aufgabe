package com.dtheng.aufgabe.taskentry.dto;

import lombok.*;

import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EntriesRequest {

	private int offset = 0;
	private int limit = 10;
	private Optional<String> taskId = Optional.empty();
}
