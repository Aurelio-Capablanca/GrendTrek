package com.aib.grendtrek.Exposure;

import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.SchemaDataMSSQL;
import com.aib.grendtrek.dataTransformations.bridge.GenerateDDLForPostgreSQL;
import com.aib.grendtrek.dataTransformations.models.requests.AttributeForConnections;
import com.aib.grendtrek.dataTransformations.services.SetUpConnections;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


import java.util.List;

@RestController
@RequestMapping("api/grendTrek/configuration")
@AllArgsConstructor
public class ConfigurationForConnections {

    public final SetUpConnections connections;
    public final GenerateDDLForPostgreSQL ddl;

    @PostMapping(value = "/setConections", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<String>>> setDatabaseConfigurations(@RequestBody List<AttributeForConnections> attributes){
        return connections.setConnections(attributes);
    }

    @PostMapping(value = "/test-ddl-generation", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<String>>> testDDL(@RequestBody List<SchemaDataMSSQL> schemaOrigin){
        return Mono.just(ResponseEntity.ok(ddl.createDDLForPostgreSQL(schemaOrigin)));
    }

}
