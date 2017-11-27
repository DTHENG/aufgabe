package com.dtheng.aufgabe.sync;

import com.dtheng.aufgabe.config.ConfigManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import retrofit.RestAdapter;
import rx.Observable;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@Singleton
public class SyncManagerImpl implements SyncManager {

    private RestAdapter restAdapter;
    private SyncClient syncClient;

    private ConfigManager configManager;

    @Inject
    public SyncManagerImpl(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public Observable<Void> startUp() {
        return configManager.getConfig()
            .filter(configuration -> configuration.getSyncRemoteIp().isPresent())
            .doOnNext(configuration ->  {
                restAdapter = new RestAdapter.Builder()
                    .setEndpoint("http://"+ configuration.getSyncRemoteIp().get())
                    .build();
                syncClient = restAdapter.create(SyncClient.class);
            })
            .ignoreElements().cast(Void.class);
    }

    @Override
    public Observable<SyncClient> getSyncClient() {
        if (syncClient == null)
            return Observable.empty();
        return Observable.just(syncClient);
    }
}
