package com.dtheng.aufgabe.config;

import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.google.inject.ImplementedBy;
import rx.Observable;

import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@ImplementedBy(ConfigManagerImpl.class)
public interface ConfigManager {

	Observable<Void> load(Optional<String> customConfigFileName);

	Observable<AufgabeConfig> getConfig();
}
