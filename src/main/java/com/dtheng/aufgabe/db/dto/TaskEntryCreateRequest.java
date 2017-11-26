package com.dtheng.aufgabe.db.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
public class TaskEntryCreateRequest {

	private String description;
}
