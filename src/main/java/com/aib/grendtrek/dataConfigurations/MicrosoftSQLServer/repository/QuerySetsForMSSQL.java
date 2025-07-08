package com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.repository;

import com.aib.grendtrek.common.GeneralResponse;
import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.MSSQLForeignKeySet;
import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.SchemaDataMSSQL;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class QuerySetsForMSSQL {


    public Flux<GeneralResponse<String>> getTableRows(ConnectionFactory factory, String tableName) {
        return Flux.usingWhen(
                Mono.from(factory.create()),
                connection -> Flux.from(connection
                                .createStatement(String.format("Select * From %s", tableName))
                                .execute())
                        .flatMap(result -> result.map(dataset -> {
                            System.out.println(dataset);
                            return new GeneralResponse<>(true, List.of("Data Fetched"), null);
                        }))
                        .onErrorResume(err -> Mono.just(new GeneralResponse<>(false, Collections.emptyList(), err.getMessage()))),
                Connection::close
        );
    }


    public Flux<GeneralResponse<String>> getAllSchemas(ConnectionFactory factory) {
        return Flux.usingWhen(
                Mono.from(factory.create()),
                connection -> Flux.from(
                                connection
                                        .createStatement("""
                                                SELECT SCHEMA_NAME from INFORMATION_SCHEMA.SCHEMATA s
                                                where s.SCHEMA_OWNER = 'dbo'
                                                """)
                                        .execute())
                        .flatMap(result -> result
                                .map((data, metadata) -> new GeneralResponse<>(true,
                                        List.of(Optional.ofNullable(data.get("SCHEMA_NAME", String.class)).orElse("Not Found")),
                                        "")
                                )
                        )
                        .onErrorResume(err ->
                                Flux.just(new GeneralResponse<>(false, Collections.emptyList(), err.getMessage()))),
                Connection::close
        );
    }

    public Flux<List<SchemaDataMSSQL>> seeAllTablesBySchema(ConnectionFactory factory, String schemaName) {
        return Flux.usingWhen(
                Mono.from(factory.create()),
                connection -> Flux.from(
                                connection.createStatement(String.format("""                    
                                                SELECT
                                                    IC.COLUMN_NAME,
                                                    IC.Data_TYPE,
                                                    IC.CHARACTER_MAXIMUM_LENGTH as LengthField,
                                                    CAST(EP.[Value] as Nvarchar) as [MS_Description],
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
                                                    INNER JOIN INFORMATION_SCHEMA.TABLES t  ON IC.TABLE_NAME = t.TABLE_NAME
                                                WHERE
                                                  IC.TABLE_CATALOG = 'AdventureWorks2022'
                                                  and IC.TABLE_SCHEMA = '%s'
                                                  and t.TABLE_TYPE = 'BASE TABLE'
                                                order by t.TABLE_NAME
                                                """, schemaName))
                                        .execute())
                        .flatMap(result -> result.map(row ->
                                        List.of(SchemaDataMSSQL.builder()
                                                .ColumnName(row.get("COLUMN_NAME", String.class))
                                                .DataType(row.get("Data_TYPE", String.class))
                                                .LenghtField(row.get("LengthField", Integer.class))
                                                .Description(row.get("MS_Description", String.class))
                                                .ConstraintName(row.get("CONSTRAINT_NAME", String.class))
                                                .ConstraintType(row.get("CONSTRAINT_TYPE", String.class))
                                                .IsNullable(row.get("IS_NULLABLE", String.class))
                                                .TableName(row.get("TABLE_NAME", String.class))
                                                .build())
                                )
                        ),
                Connection::close
        );
    }


    public Flux<List<MSSQLForeignKeySet>> getForeignKeyData(ConnectionFactory factory) {
        return Flux.usingWhen(
                Mono.from(factory.create()),
                connection -> Flux.from(connection.createStatement("""
                                        SELECT
                                        fk.name AS ForeignKeyName,
                                        sch1.name AS SourceSchema,
                                        tab1.name AS SourceTable,
                                        col1.name AS SourceColumn,
                                        sch2.name AS TargetSchema,
                                        tab2.name AS TargetTable,
                                        col2.name AS TargetColumn
                                    FROM
                                        sys.foreign_keys fk
                                    INNER JOIN sys.foreign_key_columns fkc\s
                                        ON fkc.constraint_object_id = fk.object_id
                                    INNER JOIN sys.tables tab1\s
                                        ON tab1.object_id = fk.parent_object_id
                                    INNER JOIN sys.schemas sch1\s
                                        ON tab1.schema_id = sch1.schema_id
                                    INNER JOIN sys.columns col1\s
                                        ON col1.column_id = fkc.parent_column_id AND col1.object_id = tab1.object_id
                                    INNER JOIN sys.tables tab2\s
                                        ON tab2.object_id = fk.referenced_object_id
                                    INNER JOIN sys.schemas sch2\s
                                        ON tab2.schema_id = sch2.schema_id
                                    INNER JOIN sys.columns col2\s
                                        ON col2.column_id = fkc.referenced_column_id AND col2.object_id = tab2.object_id
                                """).execute())
                        .flatMap(result -> result.map(row ->
                                List.of(MSSQLForeignKeySet.builder()
                                        .ForeignKeyName(row.get("ForeignKeyName", String.class))
                                        .SourceSchema(row.get("SourceSchema", String.class))
                                        .SourceTable(row.get("SourceTable", String.class))
                                        .SourceColumn(row.get("SourceColumn", String.class))
                                        .TargetSchema(row.get("TargetSchema", String.class))
                                        .TargetTable(row.get("TargetTable", String.class))
                                        .TargetColumn(row.get("TargetColumn", String.class))
                                        .build())
                        )),
                Connection::close
        );
    }

    public Flux<List<SchemaDataMSSQL>> seeByTableAndSchema(ConnectionFactory factory, String tableName, String schemaName) {
        return Flux.usingWhen(
                Mono.from(factory.create()),
                connection -> Flux.from(connection.createStatement(String.format("""
                                    SELECT
                                    IC.COLUMN_NAME,
                                    IC.Data_TYPE,
                                    IC.CHARACTER_MAXIMUM_LENGTH as LengthField,
                                    CAST(EP.[Value] as Nvarchar) as [MS_Description],
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
                                    INNER JOIN INFORMATION_SCHEMA.TABLES t  ON IC.TABLE_NAME = t.TABLE_NAME
                                WHERE
                                  IC.TABLE_CATALOG = 'AdventureWorks2022'
                                  and IC.TABLE_SCHEMA = '%s'
                                and IC.TABLE_NAME = '%s'
                                and t.TABLE_TYPE = 'BASE TABLE'
                                order by IC.ORDINAL_POSITION
                                """, schemaName, tableName))
                        .execute()).flatMap(result -> result.map(row ->
                        List.of(SchemaDataMSSQL.builder()
                                .ColumnName(row.get("COLUMN_NAME", String.class))
                                .DataType(row.get("Data_TYPE", String.class))
                                .LenghtField(row.get("LengthField", Integer.class))
                                .Description(row.get("MS_Description", String.class))
                                .ConstraintName(row.get("CONSTRAINT_NAME", String.class))
                                .ConstraintType(row.get("CONSTRAINT_TYPE", String.class))
                                .IsNullable(row.get("IS_NULLABLE", String.class))
                                .TableName(row.get("TABLE_NAME", String.class))
                                .build())
                )),
                Connection::close
        );
    }


}
