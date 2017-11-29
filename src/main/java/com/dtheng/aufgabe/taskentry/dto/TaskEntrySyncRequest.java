package com.dtheng.aufgabe.taskentry.dto;

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
public class TaskEntrySyncRequest {

    private String id;
    private long createdAt;
    private String taskId;
    private String inputId;
}
