package com.aib.grendtrek.dataConfigurations.PostgreSQL;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

public class PostgreSQLConnector {

    private final ConnectionFactory postgreSQLfactory;


    public PostgreSQLConnector() {
        this.postgreSQLfactory = createFactoryForPostgreSQL();
    }


    public ConnectionFactory createFactoryForPostgreSQL() {
        return ConnectionFactories.get(
                ConnectionFactoryOptions.builder()
                        .option(DRIVER, "postgresql")
                        .option(HOST, "localhost")
                        .option(PORT, 5432)
                        .option(USER, "superuserp")
                        .option(PASSWORD, "jkl555")
                        .option(DATABASE, "postgres")
                        .build()
        );
    }

    public void init() {
        System.out.println("Enters!");
        Mono.from(postgreSQLfactory.create())
                .flatMapMany(connection ->
                        Flux.from(connection
                                        .createStatement("Select 1")
                                        .execute()
                                )
                                .flatMap(result -> result.map((row, data) -> row.get(0)))
                                .doFinally(signal -> connection.close())
                )
                .doOnNext(result -> System.out.println("Query Result ! "+result))
                .doOnError(e -> System.err.println("Error: " + e.getMessage()))
                .subscribe();
    }

}
