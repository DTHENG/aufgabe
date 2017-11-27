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
public class TaskSyncRequest {

    private String id;
    private long createdAt;
    private String description;
}
