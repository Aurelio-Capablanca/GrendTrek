package com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MSSQLForeignKeySet {

    private String ForeignKeyName;
    private String SourceSchema;
    private String SourceTable;
    private String SourceColumn;
    private String TargetSchema;
    private String TargetTable;
    private String TargetColumn;

}
