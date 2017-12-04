package com.dtheng.aufgabe.security.exception;

import com.dtheng.aufgabe.exceptions.AufgabeException;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class MissingPublicKey extends AufgabeException {

    public MissingPublicKey() {
        super("Missing \"Public-Key\" header in request");
    }
}
