package com.dtheng.aufgabe.security.exception;

import com.dtheng.aufgabe.exceptions.AufgabeException;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class MissingSignature extends AufgabeException {

    public MissingSignature() {
        super("Missing \"Signature\" header in request");
    }
}