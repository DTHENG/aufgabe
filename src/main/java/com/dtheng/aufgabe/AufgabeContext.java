package com.dtheng.aufgabe;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Getter
@Setter
@Singleton
public class AufgabeContext {

    private Injector injector;
}
