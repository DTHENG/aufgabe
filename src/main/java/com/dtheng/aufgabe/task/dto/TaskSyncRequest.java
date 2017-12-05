package com.dtheng.aufgabe.task.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskSyncRequest {

    private String id;
    private long createdAt;
    private String description;
    private String bonuslyMessage;
}
