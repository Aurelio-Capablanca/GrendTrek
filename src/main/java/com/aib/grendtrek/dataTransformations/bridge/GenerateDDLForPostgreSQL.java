package com.aib.grendtrek.dataTransformations.bridge;

import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.SchemaDataMSSQL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenerateDDLForPostgreSQL {

    public List<String> createDDLForPostgreSQL(List<SchemaDataMSSQL> schemaTable) {
        final Map<String, List<SchemaDataMSSQL>> tablesPerSchema = schemaTable
                .stream()
                .collect(Collectors
                        .toMap(SchemaDataMSSQL::getTableName,
                                List::of
                        )
                );
        final List<String> DDLToCreate = new ArrayList<>();
        tablesPerSchema.forEach((key, value) -> {
            final StringBuilder DDLForTables = new StringBuilder()
                    .append("create table ").append(key)
                    .append("( ");
            value.forEach(fields -> {
                // ColumnName DataType(length) CONSTRAINT ConstraintName ConstraintType Not null (IsNullable),
                DDLForTables.append(fields.getColumnName()).append(" ")
                        .append(fields.getDataType());
                if (fields.getLenghtField() == null)
                    DDLForTables.append("(").append(fields.getLenghtField()).append(") ");
                else
                    DDLForTables.append(" ");
                //if ()
            });
            DDLToCreate.add(DDLForTables.toString());
        });
        DDLToCreate.forEach(System.out::println);
        return Collections.emptyList();
    }


}
