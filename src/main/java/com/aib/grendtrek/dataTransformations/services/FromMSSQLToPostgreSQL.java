package com.aib.grendtrek.dataTransformations.services;

import com.aib.grendtrek.common.R2DBCConnectionFactory;
import com.aib.grendtrek.common.GeneralResponse;
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
        //First: Call the Schemas from Origin
        return mssqlQueries.getAllSchemas(factory.getConnectionFactory(Origin))
                .flatMap(data -> postgreSQLQueries.createNewSchemas(factory.getConnectionFactory(Destiny), data.data())) //Second: Create the Schemas
                .flatMap(response -> Flux.fromIterable(response.data()))
                .collectList()
                .map(data -> ResponseEntity.ok(new GeneralResponse<>(true, data, null)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new GeneralResponse<>(false, Collections.emptyList(), e.getMessage()))));
    }


}
