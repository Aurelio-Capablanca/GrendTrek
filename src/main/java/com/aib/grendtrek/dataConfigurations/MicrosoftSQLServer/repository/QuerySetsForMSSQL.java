package com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.repository;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class QuerySetsForMSSQL {

    public Flux<Map<String, String>> seeAllTablesBySchema(ConnectionFactory factory) {
        return Flux.usingWhen(
                Mono.from(factory.create()),
                connection -> Flux.from(
                                connection.createStatement("""
                                                
                                                SELECT
                                                    IC.COLUMN_NAME,
                                                    IC.Data_TYPE,
                                                    IC.CHARACTER_MAXIMUM_LENGTH as LengthField,
                                                    EP.[Value] as [MS_Description],
                                                    IKU.CONSTRAINT_NAME,
                                                    ITC.CONSTRAINT_TYPE,
                                                    IC.IS_NULLABLE,
                                                    IC.TABLE_NAME
                                                 FROM
                                                    INFORMATION_SCHEMA.COLUMNS IC
                                                    INNER JOIN sys.columns sc ON OBJECT_ID(QUOTENAME(IC.TABLE_SCHEMA) + '.' + QUOTENAME(IC.TABLE_NAME)) = sc.[object_id] AND IC.COLUMN_NAME = sc.name
                                                    LEFT OUTER JOIN sys.extended_properties EP ON sc.[object_id] = EP.major_id
                                                    			AND sc.[column_id] = EP.minor_id AND EP.name = 'MS_Description' 	
                                                    			AND EP.class = 1
                                                    LEFT OUTER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE IKU ON IKU.COLUMN_NAME = IC.COLUMN_NAME
                                                    			and IKU.TABLE_NAME = IC.TABLE_NAME and IKU.TABLE_CATALOG = IC.TABLE_CATALOG
                                                    LEFT OUTER JOIN INFORMATION_SCHEMA.TABLE_CONSTRAINTS ITC ON ITC.TABLE_NAME = IKU.TABLE_NAME
                                                    			and ITC.CONSTRAINT_NAME = IKU.CONSTRAINT_NAME
                                                WHERE
                                                  IC.TABLE_CATALOG = 'AdventureWorks2022'
                                                  and IC.TABLE_SCHEMA = 'HumanResources'
                                                order by IC.ORDINAL_POSITION
                                                """)
                                        .execute())
                        .flatMap(result -> {
                            System.out.println(result);
                            return result.map((row, data) -> {
                                final String columnName = row.get("COLUMN_NAME", String.class);
                                final String tableName = row.get("TABLE_NAME", String.class);
                                return Map.of(columnName, tableName);
                            });
                        }),
                Connection::close
        );
    }





}
