package com.dtheng.aufgabe.taskentry.event;

import lombok.*;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TaskEntryCreatedEvent {

    private String id;
}
