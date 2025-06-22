package com.aib.grendtrek.dataTransformations.services;

import com.aib.grendtrek.common.R2DBCConnectionFactory;
import com.aib.grendtrek.common.ResponseActions;
import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.repository.QuerySetsForMSSQL;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class FromMSSQLToPostgreSQL {

    private final R2DBCConnectionFactory factory = R2DBCConnectionFactory.getInstance();
    private final QuerySetsForMSSQL mssqlQueries = new QuerySetsForMSSQL();

    public Mono<ResponseEntity<ResponseActions<String>>> checkAndCreateSchemas(String Origin, String Destiny) {
        //First: Get All Schemas
        return mssqlQueries.getAllSchemas(factory.getConnectionFactory(Origin))
                .flatMap(response -> Flux.fromIterable(response.data()))
                .collectList()
                .map(data -> ResponseEntity.ok(new ResponseActions<>(true, data, null)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ResponseActions<>(false, Collections.emptyList(), e.getMessage()))));
        //Second: Create the Schemas
        //Third: Get ALL Tables by Schema
        //Fourth: Create Tables at Destiny and their Foreign Keys
        //Fifth: Insert the data in those tables
        //Done!
    }


}
