package com.dtheng.aufgabe.output.bonusly;

import com.dtheng.aufgabe.AufgabeService;
import com.dtheng.aufgabe.output.bonusly.dto.BonuslyRequest;
import com.dtheng.aufgabe.config.ConfigManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import retrofit.RestAdapter;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@Singleton
public class BonuslyService implements AufgabeService {

    private RestAdapter restAdapter;
    private BonuslyApi bonuslyApi;

    private ConfigManager configManager;

    @Inject
    public BonuslyService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public Observable<Map<String, Object>> startUp() {
        return configManager.getConfig()
            .flatMap(config -> Observable.just(
                config.getBonuslyAccessToken().isPresent() &&
                config.getBonuslyApiEndpoint().isPresent() &&
                config.isBonuslyEnabled())
            .map(isBonuslyEnabled -> {
                if (isBonuslyEnabled) {
                    restAdapter = new RestAdapter.Builder()
                        .setEndpoint("https://"+ config.getBonuslyApiEndpoint().get())
                        .build();
                    bonuslyApi = restAdapter.create(BonuslyApi.class);
                }
                Map<String, Object> metaData = new HashMap<>();
                metaData.put("bonuslyEnabled", isBonuslyEnabled);
                return metaData;
            }));
    }

    @Override
    public long order() {
        return 1512028800;
    }

    public Observable<Void> send(String message) {
        return configManager.getConfig()
            .flatMap(config -> {
                if ( ! config.isBonuslyEnabled()) {
                    if (config.getBonuslyAccessToken().isPresent())
                        log.warn("Bonusly Disabled... not sending \"{}\"", message);
                    return Observable.empty();
                }
                if ( ! config.getBonuslyAccessToken().isPresent() || ! config.getBonuslyApiEndpoint().isPresent())
                    return Observable.empty();
                return bonuslyApi.create("Bearer "+ config.getBonuslyAccessToken().get(), new BonuslyRequest(message));
            })
            .ignoreElements().cast(Void.class);
    }

    public Observable<Void> test() {
        return bonuslyApi.test()
            .ignoreElements().cast(Void.class);
    }
}
