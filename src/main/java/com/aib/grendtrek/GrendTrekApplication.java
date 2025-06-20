package com.aib.grendtrek;


import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.repository.QuerySetsForMSSQL;
import com.aib.grendtrek.dataConfigurations.PostgreSQL.repository.QuerySetsForPostgreSQL;
import com.aib.grendtrek.dataConfigurations.common.ConnectionAttributes;
import com.aib.grendtrek.dataConfigurations.common.R2DBCConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;

import java.sql.SQLOutput;

@SpringBootApplication(exclude = {
        R2dbcAutoConfiguration.class
})
public class GrendTrekApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrendTrekApplication.class, args);
        final R2DBCConnectionFactory factory = new R2DBCConnectionFactory();
        //Origin
        factory.addConnections("SQLServerADworks", ConnectionAttributes.builder()
                .databaseToConnect("AdventureWorks2022")
                .host("localhost")
                .driver("mssql")
                .user("sa")
                .password("jklgHnbvc555SS")
                .port(1433)
                .build());
        //Destiny
        factory.addConnections("PostgreSQLDestiny",
                ConnectionAttributes.builder()
                        .databaseToConnect("transcontinentalshippings")
                        .host("localhost")
                        .driver("postgresql")
                        .user("superuserp")
                        .password("jkl555")
                        .port(5432)
                        .build());
        // calling them
        final ConnectionFactory origin = factory.getConnectionFactory("SQLServerADworks");
        final ConnectionFactory destiny = factory.getConnectionFactory("PostgreSQLDestiny");
        factory.init(origin);
        factory.init(destiny);
        //gettin full schemas
        final QuerySetsForMSSQL originQuerySet = new QuerySetsForMSSQL();
        final QuerySetsForPostgreSQL destinySet = new QuerySetsForPostgreSQL();

        originQuerySet.seeAllTablesBySchema(origin)
                .doOnNext(System.out::println)
                .thenMany(destinySet.seeAllTablesFromSchema(destiny).doOnNext(System.out::println))
                .thenMany(originQuerySet.seeByTableAndSchema(origin, "Employee", "HumanResources").doOnNext(System.out::println))
                .subscribe(res -> {},
                        error -> System.out.println("Error : " + error.getMessage()));
//        System.out.println("Get a Table !");
//
//        originQuerySet.seeByTableAndSchema(origin, "Employee", "HumanResources").doOnNext(System.out::println).subscribe(results -> {
//        }, error -> System.out.println("Error : " + error.getMessage()));

    }

}
