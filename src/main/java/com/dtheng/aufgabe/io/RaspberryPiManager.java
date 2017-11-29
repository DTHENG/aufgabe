package com.dtheng.aufgabe.io;

import com.google.inject.ImplementedBy;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(RaspberryPiManagerImpl.class)
public interface RaspberryPiManager {

	Observable<Void> startUp();
}
