package com.dtheng.aufgabe.taskentry.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskEntrySyncRequest {

    private String id;
    private long createdAt;
    private String taskId;
    private String inputId;
}
