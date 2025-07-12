package com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchemaDataMSSQL {

    private String ColumnName;
    private String DataType;
    private Integer LenghtField;
    private String Description;
    private String ConstraintName;
    private String ConstraintType;
    private String IsNullable;
    private String TableName;
    private Integer NumericPresicion;//(p)
    private Integer NumericScale;//(s) at NUMERIC (p,s)

}
