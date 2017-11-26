package com.dtheng.aufgabe.db.model;

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
public class TaskEntry {

    private String id;
    private Date createdAt = new Date();
    private String description;
}
