package com.dtheng.aufgabe.security.exception;

import com.dtheng.aufgabe.exceptions.AufgabeException;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class InvalidSignature extends AufgabeException {

    public InvalidSignature() {
        super("Invalid signature");
    }
}
