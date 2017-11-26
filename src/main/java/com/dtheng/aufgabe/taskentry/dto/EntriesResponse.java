package com.dtheng.aufgabe.taskentry.dto;

import com.dtheng.aufgabe.taskentry.model.TaskEntry;
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
public class EntriesResponse {

	private int offset;
	private int limit;
	private int total;
	private List<TaskEntry> entries;
}
