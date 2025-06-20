package com.aib.grendtrek.dataConfigurations.PostgreSQL.repository;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

public class QuerySetsForPostgreSQL {

    public Flux<Map<String, String>> seeAllTablesFromSchema(ConnectionFactory factory) {
        return Flux.usingWhen(
                Mono.from(factory.create()),
                connection -> Flux.from(connection.createStatement("""
                        select * from information_schema.tables;
                        """).execute()).flatMap(result -> {
                    System.out.println(result);
                    return result.map((row, value) -> {
//                        System.out.println("Row :" + row + " Value: " + value);
//                        value.getColumnMetadatas().forEach(System.out::println);
                        final String columnName = Optional.ofNullable(row.get("table_catalog", String.class)).orElse("Not Found");
                        final String tableName = Optional.ofNullable(row.get("table_name", String.class)).orElse("Not Found");
                        return Map.of(columnName, tableName);
                    });
                }),
                Connection::close
        );
    }

}
