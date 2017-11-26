package com.dtheng.aufgabe.io;

import com.google.inject.ImplementedBy;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(FileManagerImpl.class)
public interface FileManager {

	Observable<String> read(String filename);
}
