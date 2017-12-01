package com.dtheng.aufgabe.io;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.io.*;
import java.util.stream.Collectors;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class FileManagerImpl implements FileManager {

    @Override
    public Observable<String> read(String filename) {
        InputStream in = getClass().getResourceAsStream("/"+ filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        return Observable.just(reader.lines().collect(Collectors.joining("\n")));
    }
}
