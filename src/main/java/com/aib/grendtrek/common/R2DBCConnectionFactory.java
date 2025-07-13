package com.aib.grendtrek.common;

import com.aib.grendtrek.dataTransformations.models.requests.AttributeForConnections;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

public class R2DBCConnectionFactory {

    private final Map<String, SessionValues> allConnections = new HashMap<>();
    private static volatile R2DBCConnectionFactory instance;

    private R2DBCConnectionFactory() {
    }

    public static R2DBCConnectionFactory getInstance() {
        R2DBCConnectionFactory result = instance;
        if (result != null)
            return result;
        synchronized (R2DBCConnectionFactory.class){
            if (instance == null)
                instance = new R2DBCConnectionFactory();
        }
        return instance;
    }


    public void addConnections(AttributeForConnections attribute) {
        final ConnectionAttributes attributes = attribute.getAttributes();
        final ConnectionFactory factory = ConnectionFactories.get(
                ConnectionFactoryOptions.builder()
                        .option(DRIVER, attributes.getDriver())
                        .option(HOST, attributes.getHost())
                        .option(PORT, attributes.getPort())
                        .option(USER, attributes.getUser())
                        .option(PASSWORD, attributes.getPassword())
                        .option(DATABASE, attributes.getDatabaseToConnect())
                        .build()
        );
        allConnections.put(attribute.getConnectionName(), SessionValues.builder().factory(factory).isOrigin(attribute.getIsOrigin()).build());
    }

    public ConnectionFactory getConnectionFactory(String name) {
        return allConnections.get(name).getFactory();
    }

    public boolean verifyConnections() {
        return allConnections.isEmpty();
    }

    public void init(ConnectionFactory factory) {
        Flux.usingWhen(
                        Mono.from(factory.create()),
                        connection ->
                                Flux.from(connection.createStatement("SELECT 1").execute())
                                        .flatMap(result -> result.map((row, data) -> {
                                            return row.get(0) + " from : " + data.getColumnMetadatas();
                                        })),
                        connection -> Mono.from(connection.close())
                )
                .doOnNext(result -> System.out.println("Query Result : " + result))
                .subscribe(results -> {
                }, error -> System.out.println("Error at Init: " + error.getMessage()));
    }
}
