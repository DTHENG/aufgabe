package com.dtheng.aufgabe.task.model;

import lombok.*;

import java.util.Date;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Task {

	private String id;
	private Date createdAt;
	private String description;
}
