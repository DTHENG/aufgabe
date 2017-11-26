package com.dtheng.aufgabe.io;

import com.dtheng.aufgabe.taskentry.TaskEntryManager;
import com.google.inject.*;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Singleton
@Slf4j
public class RaspberryPiManagerImpl implements RaspberryPiManager {

	private TaskEntryManager taskEntryManager;

	@Inject
	public RaspberryPiManagerImpl(TaskEntryManager taskEntryManager) {
		this.taskEntryManager = taskEntryManager;
	}

	@Override
	public Observable<Void> startUp() {
		log.info("Configuring Raspberry Pi 3 IO pins...");
		log.info("Raspberry Pi 3 configured!");
		return Observable.empty();
	}
}
