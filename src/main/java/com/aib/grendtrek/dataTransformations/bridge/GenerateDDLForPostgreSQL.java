package com.aib.grendtrek.dataTransformations.bridge;

import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.SchemaDataMSSQL;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GenerateDDLForPostgreSQL {

    public List<String> createDDLForPostgreSQL(List<SchemaDataMSSQL> schemaTable) {
        final Map<String, List<SchemaDataMSSQL>> fieldsPerTable = new HashMap<>();
        schemaTable
                .forEach(data -> {
                    fieldsPerTable.computeIfAbsent(data.getTableName(), key -> new ArrayList<>()).add(data);
//                    if (fieldsPerTable.containsKey(data.getTableName())) {
//                        fieldsPerTable.get(data.getTableName()).add(data);
//                    }
//                    fieldsPerTable.put(data.getTableName(), Arrays.asList(data));
                });
        final List<String> DDLToCreate = new ArrayList<>();
        fieldsPerTable.forEach((key, value) -> {
            final StringBuilder DDLForTables = new StringBuilder()
                    .append("create table ").append(key)
                    .append("( ");
            value.forEach(fields -> {
                // ColumnName DataType(length) CONSTRAINT ConstraintName ConstraintType Not null (IsNullable),
                DDLForTables.append(fields.getColumnName()).append(" ")
                        .append(fields.getDataType());
                if (fields.getLenghtField() == null)
                    DDLForTables.append(" ");
                else
                    DDLForTables.append("(").append(fields.getLenghtField()).append(")");
                if (fields.getConstraintName() != null && fields.getConstraintType() != null) {
                    DDLForTables.append("CONSTRAINT ")
                            .append(fields.getConstraintName())
                            .append(" ")
                            .append(fields.getConstraintType())
                            .append(" ");
//                    if (fields.getConstraintType().equals("PRIMARY KEY")) {
//                        DDLForTables.append("serial ");
//                    }
                }
                DDLForTables.append(",");
            });
            DDLToCreate.add(DDLForTables.toString());
        });
        DDLToCreate.forEach(System.out::println);
        return DDLToCreate;
    }


}
