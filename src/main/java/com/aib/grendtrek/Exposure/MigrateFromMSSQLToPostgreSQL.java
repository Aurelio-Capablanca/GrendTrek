package com.aib.grendtrek.Exposure;

import com.aib.grendtrek.common.GeneralResponse;
import com.aib.grendtrek.common.requests.Schemas;
import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.MSSQLForeignKeySet;
import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.SchemaDataMSSQL;
import com.aib.grendtrek.dataTransformations.models.requests.ConnectionNames;
import com.aib.grendtrek.dataTransformations.services.FromMSSQLToPostgreSQL;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/grendTrek/migration")
@AllArgsConstructor
public class MigrateFromMSSQLToPostgreSQL {

    private final FromMSSQLToPostgreSQL migration;

    @PostMapping("/check-schema")
    public Mono<ResponseEntity<GeneralResponse<String>>> checkOriginSchemas(@RequestBody ConnectionNames names){
        return migration.checkAndCreateSchemas(names.getOrigin(), names.getDestiny());
    }

    @PostMapping("/see-table-fields-by-schemas")
    public Mono<ResponseEntity<GeneralResponse<SchemaDataMSSQL>>> checkTablesBySchema(@RequestBody Schemas schemas){
        return migration.getTablesBySchema(schemas.getOrigin(), schemas.getSchemas());
    }

    @PostMapping("/see-database-foreign-key-definitions")
    public Mono<ResponseEntity<GeneralResponse<MSSQLForeignKeySet>>> getAllForeignKeysInDatabase(@RequestBody Schemas schemas){
        return migration.getForeignKeysInDatabase(schemas.getOrigin());
    }

}
