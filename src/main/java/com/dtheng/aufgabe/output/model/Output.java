package com.dtheng.aufgabe.output.model;

import com.dtheng.aufgabe.output.OutputHandler;
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
public class Output {

    private String id;
    private Date createdAt;
    private String  taskId;
    private String deviceId;
    private Optional<Date> removedAt = Optional.empty();
    private Class<? extends OutputHandler> handler;
}
