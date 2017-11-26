package com.dtheng.aufgabe.config;

import com.dtheng.aufgabe.config.model.Configuration;
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

	private Optional<Configuration> configuration = Optional.empty();

	private FileManager fileManager;

	@Inject
	public ConfigManagerImpl(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	@Override
	public Observable<Void> load(Optional<String> customConfigFileName) {
		log.info("Loading Configuration...");
		String filename = ! customConfigFileName.isPresent() ? "configuration-default.json" : customConfigFileName.get();
		return fileManager.read(filename)
				.flatMap(raw -> {
					ObjectMapper objectMapper = new ObjectMapper();
					try {
						return Observable.just(objectMapper.readValue(raw, Configuration.class));
					} catch (IOException e) {
						return Observable.error(e);
					}
				})
				.doOnNext(configuration -> this.configuration = Optional.of(configuration))
				.doOnNext(configuration -> log.info("Configuration loaded! (/src/main/resources/{})", filename))
				.ignoreElements().cast(Void.class);
	}

	@Override
	public Observable<Configuration> getConfig() {
		if ( ! configuration.isPresent())
			return Observable.error(new RuntimeException("ConfigManager.load not called!"));
		return Observable.just(configuration.get());
	}
}
