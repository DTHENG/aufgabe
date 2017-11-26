package com.dtheng.aufgabe.db.dto;

import lombok.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EntriesRequest {

	private int offset;
	private int limit;
}
