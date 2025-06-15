package com.aib.grendtrek;


import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.repository.QuerySetsForSQLServer;
import com.aib.grendtrek.dataConfigurations.common.ConnectionAttributes;
import com.aib.grendtrek.dataConfigurations.common.Connector;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;

@SpringBootApplication(exclude = {
        R2dbcAutoConfiguration.class
})
public class GrendTrekApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrendTrekApplication.class, args);
        final Connector connector = new Connector();
        connector.addConnections("SQLServerADworks", ConnectionAttributes.builder()
                        .databaseToConnect("AdventureWorks2022")
                        .host("localhost")
                        .driver("mssql")
                        .user("sa")
                        .password("jklgHnbvc555SS")
                        .port(1433)
                .build());
        final ConnectionFactory connection = connector.getConnectionFactory("SQLServerADworks");
        connector.init(connection);
        final QuerySetsForSQLServer origin = new QuerySetsForSQLServer();
        origin.seeAllTablesBySchema(connection).doOnNext(System.out::println).subscribe(res -> {}, error -> System.out.println("Error : "+error.getMessage()));
    }

}
