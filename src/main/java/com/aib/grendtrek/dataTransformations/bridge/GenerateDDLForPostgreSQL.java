package com.aib.grendtrek.dataTransformations.bridge;

import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.SchemaDataMSSQL;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GenerateDDLForPostgreSQL {

    private static final Map<String, String> typeTranslation = Map.of(
            "nvarchar", "VARCHAR",
            "varchar", "VARCHAR",
            "int", "INTEGER",
            "tinyint", "SMALLINT",
            "datetime", "TIMESTAMP",
            "xml", "TEXT"
    );

    private String buildColumn(SchemaDataMSSQL fields){
        final StringBuilder DDLForTables = new StringBuilder();
        DDLForTables
                .append("\"").append(fields.getColumnName().replace(" ", "_")).append("\"")
                .append(" ")
                .append(typeTranslation.get(fields.getDataType()));

        if (fields.getLenghtField() != null && fields.getLenghtField() > 0)
            DDLForTables.append("(").append(fields.getLenghtField()).append(")");

        if (fields.getConstraintName() != null && fields.getConstraintType() != null) {
            DDLForTables.append(" CONSTRAINT ")
                    .append(fields.getConstraintName())
                    .append(" ")
                    .append(fields.getConstraintType())
                    .append(" ");
        }

        if("NO".equalsIgnoreCase(fields.getIsNullable()))
            DDLForTables.append(" NOT NULL");
        return DDLForTables.toString();
    }

    public List<String> createDDLForPostgreSQL(List<SchemaDataMSSQL> schemaTable) {
        final Map<String, List<SchemaDataMSSQL>> fieldsPerTable = new HashMap<>();
        schemaTable
                .forEach(data -> {
                    fieldsPerTable.computeIfAbsent(data.getTableName(), key -> new ArrayList<>()).add(data);
                });
        final List<String> DDLToCreate = new ArrayList<>();
        fieldsPerTable.forEach((key, value) -> {
            final StringBuilder DDLForTables = new StringBuilder()
                    .append("create table ")
                    .append("\"").append(key).append("\"")
                    .append("( ");
            final String columns = value.stream().map(this::buildColumn).collect(Collectors.joining(", "));
            DDLForTables.append(columns).append(" );");
            DDLToCreate.add(DDLForTables.toString());
        });
        DDLToCreate.forEach(System.out::println);
        System.out.println("TABLES to create : "+DDLToCreate.size());
        return DDLToCreate;
    }


}
