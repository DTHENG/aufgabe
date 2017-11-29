package com.dtheng.aufgabe.stats.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StatsDefaultResponse {

    private List<String> activeInputs = new ArrayList<>();
    private List<JsonNode> last10Entries = new ArrayList<>();
}
