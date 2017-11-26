package com.dtheng.aufgabe.button.dto;

import com.dtheng.aufgabe.button.model.Button;
import lombok.*;

import java.util.List;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ButtonsResponse {

    private int offset;
    private int limit;
    private int total;
    private List<Button> buttons;
}
