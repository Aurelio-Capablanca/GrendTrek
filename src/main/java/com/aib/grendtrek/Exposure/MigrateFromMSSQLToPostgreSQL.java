package com.aib.grendtrek.Exposure;

import com.aib.grendtrek.common.ResponseActions;
import com.aib.grendtrek.dataTransformations.models.requests.ConnectionNames;
import com.aib.grendtrek.dataTransformations.services.FromMSSQLToPostgreSQL;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/grendTrek/migration")
@AllArgsConstructor
public class MigrateFromMSSQLToPostgreSQL {

    private final FromMSSQLToPostgreSQL migration = new FromMSSQLToPostgreSQL();

    @PostMapping("/check-schema")
    public Mono<ResponseEntity<ResponseActions<String>>> checkOriginSchemas(@RequestBody ConnectionNames names){
        return migration.checkAndCreateSchemas(names.getOrigin(), names.getDestiny());
    }

}
