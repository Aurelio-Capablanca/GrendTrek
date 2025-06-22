package com.aib.grendtrek.dataTransformations.services;

import com.aib.grendtrek.common.R2DBCConnectionFactory;
import com.aib.grendtrek.dataTransformations.models.requests.AttributeForConnections;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class SetUpConnections {

    private final R2DBCConnectionFactory factory = R2DBCConnectionFactory.getInstance();

    public Mono<ResponseEntity<List<String>>> setConnections(List<AttributeForConnections> attributes) {
        return Flux.fromIterable(attributes)
                .flatMap(connections -> Mono.fromCallable(() -> {
                                    factory.addConnections(connections);
                                    factory.init(factory.getConnectionFactory(connections.getConnectionName()));
                                    return "Connection : " + connections.getConnectionName() + " added!";
                                })
                                .onErrorResume(exception -> Mono.just("Error at : " + connections.getConnectionName() + " : " + exception.getMessage()))
                ).collectList().map(ResponseEntity::ok);
    }


}
