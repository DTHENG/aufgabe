package com.dtheng.aufgabe.sync.model;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SyncEntry {

    private String id;
    private Date createdAt;
    private Optional<Date> startedAt = Optional.empty();
    private Optional<Date> completedAt = Optional.empty();
    private Optional<Integer> numberOfRecordsSynced = Optional.empty();
    private Optional<List<String>> recordsSynced = Optional.empty();
}
