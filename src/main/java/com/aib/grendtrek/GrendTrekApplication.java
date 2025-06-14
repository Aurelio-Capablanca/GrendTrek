package com.aib.grendtrek;

import com.aib.grendtrek.dataConfigurations.PostgreSQL.PostgreSQLConnector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;

@SpringBootApplication(exclude = {
        R2dbcAutoConfiguration.class
})
public class GrendTrekApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrendTrekApplication.class, args);
        PostgreSQLConnector postgre = new PostgreSQLConnector();
        postgre.init();
    }

}
