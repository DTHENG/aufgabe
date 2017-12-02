package com.dtheng.aufgabe.task.dto;

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
public class TaskUpdateRequest {

    private Optional<String> description = Optional.empty();
    private Optional<String> bonuslyMessage = Optional.empty();
}
