package com.dtheng.aufgabe.bonusly;

import com.dtheng.aufgabe.AufgabeService;
import com.dtheng.aufgabe.bonusly.dto.BonuslyRequest;
import com.dtheng.aufgabe.bonusly.dto.BonuslyResponse;
import com.dtheng.aufgabe.config.ConfigManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import rx.Observable;

import java.util.Optional;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@Singleton
public class BonuslyService implements AufgabeService {

    private Optional<String> accessToken = Optional.empty();
    private Optional<String> endpoint = Optional.empty();

    private RestAdapter restAdapter;
    private BonuslyApi bonuslyApi;

    private ConfigManager configManager;

    @Inject
    public BonuslyService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public Observable<Void> startUp() {
        return configManager.getConfig()
            .doOnNext(config -> {
                this.accessToken = config.getBonuslyAccessToken();
                this.endpoint = config.getBonuslyApiEndpoint();
            })
            .filter(Void -> this.accessToken.isPresent() && this.endpoint.isPresent())
            .doOnNext(Void -> {
                restAdapter = new RestAdapter.Builder()
                    .setEndpoint("https://"+ this.endpoint.get())
                    .build();
                bonuslyApi = restAdapter.create(BonuslyApi.class);
            })
            .ignoreElements().cast(Void.class);
    }

    public Observable<Void> send(String message) {
        if ( ! this.accessToken.isPresent() || ! this.endpoint.isPresent())
            return Observable.empty();
        return bonuslyApi.create("Bearer "+ this.accessToken.get(), new BonuslyRequest(message))
            .onErrorResumeNext(throwable -> {
                if (throwable instanceof RetrofitError) {
                    RetrofitError retrofitError = (RetrofitError) throwable;
                    BonuslyResponse response = (BonuslyResponse) retrofitError.getBodyAs(BonuslyResponse.class);
                    log.error(response.getMessage());
                    return Observable.empty();
                }
                return Observable.error(throwable);
            })
            .ignoreElements().cast(Void.class);
    }
}
