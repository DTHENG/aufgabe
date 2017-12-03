package com.dtheng.aufgabe.jooq;

import com.dtheng.aufgabe.AufgabeService;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
@Singleton
public class JooqServiceImpl implements JooqService, AufgabeService {

    private Configuration configuration;

    private ConfigManager configManager;

    @Inject
    public JooqServiceImpl(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public Observable<Map<String, Object>> startUp() {
        return connect();
    }

    @Override
    public long order() {
        return 1506903600;
    }

    @Override
    public Observable<DSLContext> getConnection() {
        if (configuration == null)
            return Observable.error(new RuntimeException("JooqService configuration is null!"));
        return Observable.just(DSL.using(configuration));
    }

    @Override
    public Observable<DSLContext> reconnect() {
        return connect()
            .flatMap(Void -> getConnection());
    }

    private Observable<Map<String, Object>> connect() {
        return configManager.getConfig()
            .flatMap(config -> {
                String dbUrl = "jdbc:mysql://localhost:" + config.getDatabasePort() + "/" + config.getDatabaseName();
                return createConnection(dbUrl, config)
                    .doOnNext(connection ->
                        configuration = new DefaultConfiguration()
                            .set(connection)
                            .set(SQLDialect.MYSQL))
                    .map(Void -> {
                        Map<String, Object> metaData = new HashMap<>();
                        metaData.put("database", dbUrl);
                        return metaData;
                    });
            });
    }

    private Observable<Connection> createConnection(String url, AufgabeConfig config) {
        try {
            return Observable.just(DriverManager.getConnection(url, config.getDatabaseUser(), config.getDatabasePassword()));
        } catch (SQLException se) {
            return Observable.error(se);
        }
    }
}