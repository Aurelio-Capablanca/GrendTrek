package com.aib.grendtrek.dataConfigurations.common;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

public class Connector {

    public final Map<String, ConnectionFactory> allConnections = new HashMap<>();

    public Connector() {
    }

    public void addConnections(String name, ConnectionAttributes attributes) {
        final ConnectionFactory factory = ConnectionFactories.get(
                ConnectionFactoryOptions.builder()
                        .option(DRIVER, attributes.getDriver()/*"postgresql"*/)
                        .option(HOST, attributes.getHost() /*"localhost"*/)
                        .option(PORT, attributes.getPort()/*5432*/)
                        .option(USER, attributes.getUser()/*"superuserp"*/)
                        .option(PASSWORD, attributes.getPassword()/*"jkl555"*/)
                        .option(DATABASE, attributes.getDatabaseToConnect()/*"postgres"*/)
                        .build()
        );
        allConnections.put(name, factory);
    }

    public ConnectionFactory getConnectionFactory(String name) {
        return allConnections.get(name);
    }

    public void init(ConnectionFactory factory) {
        System.out.println("Enters!");
        Mono.from(factory.create())
                .flatMapMany(connection ->
                        Flux.from(connection
                                        .createStatement("SELECT 1")
                                        .execute()
                                )
                                .flatMap(result -> result.map((row, data) -> row.get(0)))
                                .doFinally(signal -> connection.close())
                )
                .doOnNext(result -> System.out.println("Query Result ! " + result))
                .doOnError(e -> System.err.println("Error: " + e.getMessage()))
                .subscribe(result -> {
                }, error -> System.out.println("Subscriber Error: " + error.getMessage()));
    }
}
