package com.dtheng.aufgabe.stats.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, Integer> totals = new HashMap<>();
}
