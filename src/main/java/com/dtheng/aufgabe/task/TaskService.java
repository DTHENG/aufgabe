package com.dtheng.aufgabe.task;

import com.dtheng.aufgabe.AufgabeService;
import com.dtheng.aufgabe.bonusly.BonuslyService;
import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.event.EventManager;
import com.dtheng.aufgabe.task.dto.AggregateTask;
import com.dtheng.aufgabe.taskentry.TaskEntryManager;
import com.dtheng.aufgabe.taskentry.event.TaskEntryCreatedEvent;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import retrofit.RetrofitError;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class TaskService implements AufgabeService {

    private EventManager eventManager;
    private ConfigManager configManager;
    private TaskEntryManager taskEntryManager;
    private TaskManager taskManager;
    private BonuslyService bonuslyService;

    @Inject
    public TaskService(EventManager eventManager, ConfigManager configManager, TaskEntryManager taskEntryManager, TaskManager taskManager, BonuslyService bonuslyService) {
        this.eventManager = eventManager;
        this.configManager = configManager;
        this.taskEntryManager = taskEntryManager;
        this.taskManager = taskManager;
        this.bonuslyService = bonuslyService;
    }

    @Override
    public Observable<Void> startUp() {
        return configManager.getConfig()
            .flatMap(config -> {
                switch (config.getDeviceType()) {
                    case EC2_INSTANCE:
                        eventManager.getTaskEntryCreated()
                            .addListener((TaskEntryCreatedEvent event) -> onTaskEntryCreated(event.getId())
                                .subscribe(Void -> {}, error -> log.error(error.toString())));
                        return Observable.empty();
                    default:
                        return Observable.empty();
                }
            });
    }

    private Observable<Void> onTaskEntryCreated(String id) {
        return taskEntryManager.get(id)
            .flatMap(taskEntry -> taskManager.get(taskEntry.getTaskId()))
            .map(AggregateTask::getTask)
            .filter(task -> task.getBonuslyMessage().isPresent())
            .flatMap(task -> bonuslyService.send(task.getBonuslyMessage().get()))
            .onErrorResumeNext(throwable -> {
                if (throwable instanceof RetrofitError) {
                    log.info("Got error from bonusly...");
                    throwable.printStackTrace();
                    return Observable.empty();
                }
                return Observable.error(throwable);
            });
    }

}
