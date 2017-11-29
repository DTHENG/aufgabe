package com.dtheng.aufgabe.jooq;

import com.dtheng.aufgabe.config.ConfigManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import rx.Observable;

import java.sql.DriverManager;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@Singleton
public class JooqManagerImpl implements JooqManager {

    private Configuration configuration;

    private ConfigManager configManager;

    @Inject
    public JooqManagerImpl(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public Observable<Void> startUp() {
        return configManager.getConfig()
            .flatMap(config -> {
                String url = "jdbc:mysql://localhost:"+ config.getDatabasePort() +"/"+ config.getDatabaseName();
                try {
                    configuration = new DefaultConfiguration()
                        .set(DriverManager.getConnection(url, config.getDatabaseUser(), config.getDatabasePassword()))
                        .set(SQLDialect.MYSQL);
                    return Observable.empty();
                } catch (Exception e) {
                    return Observable.error(e);
                }
            });
    }

    @Override
    public Observable<DSLContext> getConnection() {
        return Observable.just(DSL.using(configuration));
    }

    @Override
    public Observable<DSLContext> reconnect() {
        return startUp()
            .defaultIfEmpty(null)
            .flatMap(Void -> getConnection());
    }
}