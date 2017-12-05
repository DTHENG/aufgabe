package com.dtheng.aufgabe.output.exception;

import com.dtheng.aufgabe.exceptions.AufgabeException;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class OutputNotFoundException extends AufgabeException {

    public OutputNotFoundException(String id) {
        super("Output not found, id: "+ id);
    }
}