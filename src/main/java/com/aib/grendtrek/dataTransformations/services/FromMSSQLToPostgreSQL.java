package com.aib.grendtrek.dataTransformations.services;

import com.aib.grendtrek.common.R2DBCConnectionFactory;
import com.aib.grendtrek.common.ResponseActions;
import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.repository.QuerySetsForMSSQL;
import com.aib.grendtrek.dataConfigurations.PostgreSQL.repository.QuerySetsForPostgreSQL;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class FromMSSQLToPostgreSQL {

    private final R2DBCConnectionFactory factory = R2DBCConnectionFactory.getInstance();
    private final QuerySetsForMSSQL mssqlQueries;
    private final QuerySetsForPostgreSQL postgreSQLQueries;

    public Mono<ResponseEntity<ResponseActions<String>>> checkAndCreateSchemas(String Origin, String Destiny) {
        return mssqlQueries.getAllSchemas(factory.getConnectionFactory(Origin))
                .flatMap(data -> postgreSQLQueries.createNewSchemas(factory.getConnectionFactory(Destiny), data.data())) //Second: Create the Schemas
                .flatMap(response -> Flux.fromIterable(response.data()))
                .collectList()
                .map(data -> ResponseEntity.ok(new ResponseActions<>(true, data, null)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ResponseActions<>(false, Collections.emptyList(), e.getMessage()))));
    }


}
