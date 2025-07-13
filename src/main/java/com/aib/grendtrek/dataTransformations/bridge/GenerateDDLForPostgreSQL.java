package com.aib.grendtrek.dataTransformations.bridge;

import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.MSSQLForeignKeySet;
import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.SchemaDataMSSQL;
import com.aib.grendtrek.dataTransformations.models.requests.DDLManagement;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GenerateDDLForPostgreSQL {

    private static final List<String> precicionTypes = List.of("NUMERIC", "DECIMAL");
    private static final Map<String, String> typeTranslation = Stream.of(new String[][]{
                    {"nvarchar", "VARCHAR"},
                    {"varchar", "VARCHAR"},
                    {"int", "INTEGER"},
                    {"tinyint", "SMALLINT"},
                    {"datetime", "TIMESTAMP"},
                    {"xml", "TEXT"},
                    {"money", "NUMERIC"},
                    {"uniqueidentifier", "UUID"},
                    {"nchar", "CHAR"},
                    {"geography", "GEOGRAPHY"},
                    {"bit", "BOOLEAN"},
                    {"smallmoney", "NUMERIC"},
                    {"decimal", "DECIMAL"},
                    {"hierarchyid", "LTREE"},
                    {"smallint", "SMALLINT"},
                    {"numeric", "NUMERIC"},
                    {"date", "DATE"},
                    {"time", "TIME"},
                    {"varbinary", "BIT"}
            })
            .collect(Collectors.toMap(data -> data[0], data -> data[1]));


    private String buildColumn(SchemaDataMSSQL fields) {
        final StringBuilder DDLForTables = new StringBuilder();
        final String isPrimaryKey = Optional.ofNullable(fields.getConstraintType()).orElse("N");
        final String typeForField = isPrimaryKey.equalsIgnoreCase("PRIMARY KEY") ? "SERIAL" : typeTranslation.get(fields.getDataType());
        DDLForTables
                .append("\"").append(fields.getColumnName().replace(" ", "_")).append("\"")
                .append(" ")
                .append(typeForField);

        if (fields.getLenghtField() != null && fields.getLenghtField() > 0)
            DDLForTables.append("(").append(fields.getLenghtField()).append(")");
        if (fields.getNumericPresicion() != null && fields.getNumericScale() != null && precicionTypes.contains(typeForField))
            DDLForTables.append("(").append(fields.getNumericPresicion()).append(",").append(fields.getNumericScale()).append(")");
        if ("NO".equalsIgnoreCase(fields.getIsNullable()))
            DDLForTables.append(" NOT NULL");
        return DDLForTables.toString();
    }

    public List<DDLManagement> createDDLForPostgreSQL(List<SchemaDataMSSQL> schemaTable) {
        final Map<String, List<SchemaDataMSSQL>> fieldsPerTable = new HashMap<>();
        schemaTable
                .forEach(data -> {
                    fieldsPerTable.computeIfAbsent(data.getTableName(), key -> new ArrayList<>()).add(data);
                });
        final List<DDLManagement> DDLToCreate = new ArrayList<>();
        fieldsPerTable.forEach((key, value) -> {
            final StringBuilder DDLForTables = new StringBuilder()
                    .append("create table ")
                    .append("\"").append(key).append("\"")
                    .append("( ");
            final String columns = value.stream().map(this::buildColumn).collect(Collectors.joining(", "));
            final String constraints = value.stream().filter(data -> data.getConstraintType() != null)
                    .filter(data -> !data.getConstraintType().equalsIgnoreCase("FOREIGN KEY"))
                    .collect(Collectors.groupingBy(SchemaDataMSSQL::getTableName))
                    .values().stream()
                    .map(schemaDataMSSQLS -> {
                        AtomicReference<String> constraintName = new AtomicReference<>();
                        AtomicReference<String> constraintType = new AtomicReference<>();
                        final String fields = schemaDataMSSQLS.stream().map(field -> {
                                    constraintType.set(field.getConstraintType());
                                    constraintName.set(field.getConstraintName());
                                    return "\"" + field.getColumnName() + "\"";
                                })
                                .collect(Collectors.joining(","));
                        return "CONSTRAINT \"" + constraintName.get() + "\" " + constraintType.get() + " (" + fields + ")";
                    })
                    .collect(Collectors.joining(""));
            DDLForTables.append(columns).append(constraints.isEmpty() ? "" : ", " + constraints).append(" );");
            DDLToCreate.add(DDLManagement.builder().DDL(DDLForTables.toString()).TableName(key).build());
        });
        //DDLToCreate.forEach(System.out::println);
        //System.out.println("TABLES to create : " + DDLToCreate.size());
        return DDLToCreate;
    }


    private String generateDMLForForeignKeys(MSSQLForeignKeySet foreignKeySet) {
        return "ALTER TABLE \"" + foreignKeySet.getSourceSchema() + "\".\"" + foreignKeySet.getSourceTable()
               + "\" ADD CONSTRAINT \"" + foreignKeySet.getForeignKeyName()
               + "\" FOREIGN KEY (\"" + foreignKeySet.getSourceColumn() + "\") REFERENCES \""
               + foreignKeySet.getTargetSchema() + "." + foreignKeySet.getTargetTable() + "\"(\""
               + foreignKeySet.getTargetColumn() + "\")";
    }

    public String generateDDLForForeignKeys(List<MSSQLForeignKeySet> foreignKeys) {
        //ALTER TABLE "ORIGIN_TABLE" ADD CONSTRAINT "CONSTRAINT_NAME"
        // FOREIGN KEY ("foreign_key_column") REFERENCES "TABLE_ORIGIN" ON DELETE SET NULL ("primary_key_column")
        return foreignKeys.stream().map(this::generateDMLForForeignKeys)
                .collect(Collectors.joining("; "));
    }

}
