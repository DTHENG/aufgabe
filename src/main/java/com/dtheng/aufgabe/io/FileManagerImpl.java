package com.dtheng.aufgabe.io;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.io.*;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class FileManagerImpl implements FileManager {

    @Override
    public Observable<String> read(String filename) {
        Optional<InputStream> in = Optional.ofNullable(getClass().getResourceAsStream("/"+ filename));
        if ( ! in.isPresent())
            return Observable.error(new FileNotFoundException(filename));
        BufferedReader reader = new BufferedReader(new InputStreamReader(in.get()));
        return Observable.just(reader.lines().collect(Collectors.joining("\n")));
    }
}
