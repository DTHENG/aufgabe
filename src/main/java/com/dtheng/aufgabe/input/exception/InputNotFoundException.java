package com.dtheng.aufgabe.input.exception;

import com.dtheng.aufgabe.exceptions.AufgabeException;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class InputNotFoundException extends AufgabeException {

    public InputNotFoundException(String id) {
        super("Input not found, id: "+ id);
    }
}
