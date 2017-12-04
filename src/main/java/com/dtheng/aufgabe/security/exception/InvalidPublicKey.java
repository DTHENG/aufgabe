package com.dtheng.aufgabe.security.exception;

import com.dtheng.aufgabe.exceptions.AufgabeException;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class InvalidPublicKey extends AufgabeException {

    public InvalidPublicKey() {
        super("Invalid public key");
    }
}
