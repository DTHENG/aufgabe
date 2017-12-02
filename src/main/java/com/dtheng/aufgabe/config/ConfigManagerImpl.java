package com.dtheng.aufgabe.config;

import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.io.FileManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@Singleton
public class ConfigManagerImpl implements ConfigManager {

    private Optional<AufgabeConfig> configuration = Optional.empty();

    private FileManager fileManager;

    @Inject
    public ConfigManagerImpl(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public Observable<String> load(Optional<String> customConfigFileName) {
        String filename = ! customConfigFileName.isPresent() ? "configuration-default.json" : customConfigFileName.get();
        return fileManager.read(filename)
            .flatMap(raw -> {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.findAndRegisterModules();
                try {
                    return Observable.just(objectMapper.readValue(raw, AufgabeConfig.class));
                } catch (IOException e) {
                    return Observable.error(e);
                }
            })
            .doOnNext(configuration -> this.configuration = Optional.of(configuration))
            .map(Void -> filename);
    }

    @Override
    public Observable<AufgabeConfig> getConfig() {
        if ( ! configuration.isPresent())
            return Observable.error(new RuntimeException("ConfigManager.load not called!"));
        return Observable.just(configuration.get());
    }
}