package com.aib.grendtrek.dataTransformations.services;

import com.aib.grendtrek.common.R2DBCConnectionFactory;
import com.aib.grendtrek.common.GeneralResponse;
import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.MSSQLForeignKeySet;
import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.SchemaDataMSSQL;
import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.repository.QuerySetsForMSSQL;
import com.aib.grendtrek.dataConfigurations.PostgreSQL.repository.QuerySetsForPostgreSQL;
import com.aib.grendtrek.dataTransformations.bridge.GenerateDDLForPostgreSQL;
import com.aib.grendtrek.dataTransformations.models.requests.DDLManagement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final GenerateDDLForPostgreSQL ddlGenerationPostgresql;

    public Mono<ResponseEntity<GeneralResponse<String>>> checkAndCreateSchemas(String Origin, String Destiny) {
        return mssqlQueries.getAllSchemas(factory.getConnectionFactory(Origin))
                .flatMap(data -> postgreSQLQueries.createNewSchemas(factory.getConnectionFactory(Destiny), data.data())) //Second: Create the Schemas
                .flatMap(response -> Flux.fromIterable(response.data()))
                .collectList()
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


    public Mono<ResponseEntity<GeneralResponse<MSSQLForeignKeySet>>> getForeignKeysInDatabase(String Origin) {
        return mssqlQueries.getForeignKeyData(factory.getConnectionFactory(Origin))
                .flatMap(Flux::fromIterable)
                .collectList()
                .map(data -> ResponseEntity.ok(new GeneralResponse<>(true,
                        data, null)))
                .onErrorResume(err -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new GeneralResponse<>(false, Collections.emptyList(), err.getMessage())))
                );
    }

    public Mono<ResponseEntity<GeneralResponse<String>>> createAllTables(List<SchemaDataMSSQL> schemaTable, String Origin){
        final List<DDLManagement> DDL = ddlGenerationPostgresql.createDDLForPostgreSQL(schemaTable);
        return postgreSQLQueries.executeTableCreations(factory.getConnectionFactory(Origin), DDL)
                .flatMap(Flux::fromIterable).collectList()
                .map(data -> ResponseEntity.ok(new GeneralResponse<>(true, data, null)))
                .onErrorResume(err -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new GeneralResponse<>(false, Collections.emptyList(), err.getMessage()))));

    }

}
