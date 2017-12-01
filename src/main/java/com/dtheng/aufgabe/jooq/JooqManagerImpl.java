package com.dtheng.aufgabe.jooq;

import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import rx.Observable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
    public Observable<Void> start() {
        return connect();
    }

    @Override
    public Observable<DSLContext> getConnection() {
        if (configuration == null)
            return connect()
                .defaultIfEmpty(null)
                .map(Void -> DSL.using(configuration));
        return Observable.just(DSL.using(configuration));
    }

    @Override
    public Observable<DSLContext> reconnect() {
        return connect()
            .defaultIfEmpty(null)
            .flatMap(Void -> getConnection());
    }

    private Observable<Void> connect() {
        return configManager.getConfig()
            .flatMap(config -> createConnection("jdbc:mysql://localhost:"+ config.getDatabasePort() +"/"+ config.getDatabaseName(), config))
            .doOnNext(connection ->
                configuration = new DefaultConfiguration()
                    .set(connection)
                    .set(SQLDialect.MYSQL))
            .ignoreElements().cast(Void.class);
    }

    private Observable<Connection> createConnection(String url, AufgabeConfig config) {
        try {
            return Observable.just(DriverManager.getConnection(url, config.getDatabaseUser(), config.getDatabasePassword()));
        } catch (SQLException se) {
            return Observable.error(se);
        }
    }
}