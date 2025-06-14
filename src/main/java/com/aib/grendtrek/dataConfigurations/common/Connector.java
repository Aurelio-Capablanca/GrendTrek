package com.aib.grendtrek.dataConfigurations.common;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
//import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

import java.util.HashMap;
import java.util.Map;

public class Connector {

//    public Map<String, ConnectionFactory> databaseConnectors = new HashMap<>();
//    private final DatabaseClient client;
//
//    public Connector(){
//        this.client = DatabaseConnector();
//    }
//
//    public DatabaseClient DatabaseConnector(){
//        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
//                .option(DRIVER, "postgresql")
//                .option(HOST, "localhost")
//                .option(PORT, 5432)
//                .option(USER, "superuserp")
//                .option(PASSWORD, "jnk555")
//                .option(DATABASE , "postgres")
//                .build();
//        ConnectionFactory factory = ConnectionFactories.get(options);
//        return DatabaseClient.builder().connectionFactory(factory).build();
//    }
//
//
//    public Mono<Void> init(){
//        return client.sql("Select 1").fetch().rowsUpdated().then();
//    }
}
