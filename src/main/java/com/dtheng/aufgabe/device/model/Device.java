package com.dtheng.aufgabe.device.model;

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
public class Device {

    private String id;
    private Date createdAt;
    private Optional<String> name = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<Date> updatedAt = Optional.empty();
    private Optional<Date> syncedAt = Optional.empty();
}
