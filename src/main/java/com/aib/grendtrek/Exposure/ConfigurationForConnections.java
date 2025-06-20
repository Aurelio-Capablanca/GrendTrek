package com.aib.grendtrek.Exposure;

import com.aib.grendtrek.dataConfigurations.common.ConnectionAttributes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


import java.util.List;

@RestController
@RequestMapping("api/grendTrek/configuration")
public class ConfigurationForConnections {

    @PostMapping(value = "/setConections", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> setDatabaseConfigurations(@RequestBody List<ConnectionAttributes> attributes){
        return null;
    }


}
