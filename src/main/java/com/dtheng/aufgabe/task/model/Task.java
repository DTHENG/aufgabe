package com.dtheng.aufgabe.task.model;

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
public class Task {

	private String id;
	private Date createdAt;
	private String description;
    private Optional<Date> updatedAt = Optional.empty();
    private Optional<Date> syncedAt = Optional.empty();
    private Optional<String> bonuslyMessage = Optional.empty();
}
