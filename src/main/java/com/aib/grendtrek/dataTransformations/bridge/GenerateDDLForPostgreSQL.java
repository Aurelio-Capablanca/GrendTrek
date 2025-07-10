package com.aib.grendtrek.dataTransformations.bridge;

import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.MSSQLForeignKeySet;
import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.SchemaDataMSSQL;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
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

    private String buildColumn(SchemaDataMSSQL fields) {
        final StringBuilder DDLForTables = new StringBuilder();
        DDLForTables
                .append("\"").append(fields.getColumnName().replace(" ", "_")).append("\"")
                .append(" ")
                .append(typeTranslation.get(fields.getDataType()));
        if (fields.getLenghtField() != null && fields.getLenghtField() > 0)
            DDLForTables.append("(").append(fields.getLenghtField()).append(")");
        if ("NO".equalsIgnoreCase(fields.getIsNullable()))
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
            final String constraints = value.stream().filter(data -> data.getConstraintType() != null)
                    .filter(data -> !data.getConstraintType().equalsIgnoreCase("FOREIGN KEY"))
                    .map(constraint -> " CONSTRAINT \"" + constraint.getConstraintName() + "\" " + constraint.getConstraintType() + " (\"" + constraint.getColumnName() + "\")")
                    .collect(Collectors.joining(", "));
            DDLForTables.append(columns).append(constraints.isEmpty() ? "" : ", " + constraints).append(" );");
            DDLToCreate.add(DDLForTables.toString());
        });
        DDLToCreate.forEach(System.out::println);
        System.out.println("TABLES to create : " + DDLToCreate.size());
        return DDLToCreate;
    }


    private String generateDMLForForeignKeys(MSSQLForeignKeySet foreignKeySet) {
        return "ALTER TABLE \"" + foreignKeySet.getSourceSchema() + "." + foreignKeySet.getSourceTable()
               + "\" ADD CONSTRAINT \"" + foreignKeySet.getForeignKeyName()
               + "\" FOREIGN KEY (\"" + foreignKeySet.getSourceColumn() + "\") REFERENCES \""
               + foreignKeySet.getTargetSchema() + "." + foreignKeySet.getTargetTable() + "\"(\""
               + foreignKeySet.getTargetColumn() + "\")";
    }

    public String generateDDLForForeignKeys(List<MSSQLForeignKeySet> foreignKeys) {
        //ALTER TABLE "ORIGIN_TABLE" ADD CONSTRAINT "CONSTRAINT_NAME"
        // FOREIGN KEY ("foreign_key_column") REFERENCES "TABLE_ORIGIN" ON DELETE SET NULL ("primary_key_column")
        final String DMLForeignKey = foreignKeys.stream().map(this::generateDMLForForeignKeys)
                .collect(Collectors.joining("; "));
        return DMLForeignKey;
    }

}
