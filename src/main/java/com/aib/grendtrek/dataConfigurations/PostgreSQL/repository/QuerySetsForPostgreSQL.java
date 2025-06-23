package com.aib.grendtrek.dataConfigurations.PostgreSQL.repository;

import com.aib.grendtrek.common.ResponseActions;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class QuerySetsForPostgreSQL {

    public Flux<Map<String, String>> seeAllTablesFromSchema(ConnectionFactory factory) {
        return Flux.usingWhen(
                Mono.from(factory.create()),
                connection -> Flux.from(connection.createStatement("""
                        select * from information_schema.tables;
                        """).execute()).flatMap(result -> {
                    System.out.println(result);
                    return result.map((row, value) -> {
                        final String columnName = Optional.ofNullable(row.get("table_catalog", String.class)).orElse("Not Found");
                        final String tableName = Optional.ofNullable(row.get("table_name", String.class)).orElse("Not Found");
                        return Map.of(columnName, tableName);
                    });
                }),
                Connection::close
        );
    }

    public Flux<ResponseActions<String>> createNewSchemas(ConnectionFactory factory, List<String> schemaNames) {
        return Flux.usingWhen(
                Mono.from(factory.create()),
                connection -> Flux.fromIterable(schemaNames)
                        .flatMap(schemaName ->
                                connection.createStatement(String.format("CREATE SCHEMA IF NOT EXISTS %s;", schemaName))
                                        .execute()
                        ).flatMap(result -> result
                                .map((data, metadata) ->
                                        new ResponseActions<>(true, List.of(""), "")))
                        .onErrorResume(err -> Flux.just(new ResponseActions<>(false , Collections.emptyList(), err.getMessage()))),
                Connection::close
        );
    }

}
