package com.aib.grendtrek.dataTransformations.services;

import com.aib.grendtrek.common.R2DBCConnectionFactory;
import com.aib.grendtrek.common.GeneralResponse;
import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.SchemaDataMSSQL;
import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.repository.QuerySetsForMSSQL;
import com.aib.grendtrek.dataConfigurations.PostgreSQL.repository.QuerySetsForPostgreSQL;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FromMSSQLToPostgreSQL {

    private final R2DBCConnectionFactory factory = R2DBCConnectionFactory.getInstance();
    private final QuerySetsForMSSQL mssqlQueries;
    private final QuerySetsForPostgreSQL postgreSQLQueries;

    public Mono<ResponseEntity<GeneralResponse<String>>> checkAndCreateSchemas(String Origin, String Destiny) {
        return mssqlQueries.getAllSchemas(factory.getConnectionFactory(Origin))
                .doOnNext(schemas -> System.out.println("Schemas found: " + schemas))
                .flatMap(data -> postgreSQLQueries.createNewSchemas(factory.getConnectionFactory(Destiny), data.data())) //Second: Create the Schemas
                .doOnNext(created -> System.out.println("Schemas created: " + created))
                .flatMap(response -> Flux.fromIterable(response.data()))
                .doOnNext(item -> System.out.println("Item in flux: " + item))
                .collectList()
                .doOnNext(list -> System.out.println("Final list: " + list))
                .map(data -> ResponseEntity.ok(new GeneralResponse<>(true, data, null)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new GeneralResponse<>(false, Collections.emptyList(), e.getMessage()))));
    }

    public Mono<ResponseEntity<GeneralResponse<SchemaDataMSSQL>>> getTablesBySchema(String Origin, List<String> schemas) {
        return Flux.fromIterable(schemas)
                .flatMap(data ->
                        mssqlQueries.seeAllTablesBySchema(factory.getConnectionFactory(Origin), data)
                )
                .flatMap(Flux::fromIterable)
                .collectList()
                .map(data -> ResponseEntity.ok(new GeneralResponse<>(true,
                        data, null)))
                .onErrorResume(err -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR
                        ).body(new GeneralResponse<>(false, Collections.emptyList(), err.getMessage())))
                );
    }

}
