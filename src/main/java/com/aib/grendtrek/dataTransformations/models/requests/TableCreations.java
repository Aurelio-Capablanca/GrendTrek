package com.aib.grendtrek.dataTransformations.models.requests;

import com.aib.grendtrek.dataConfigurations.MicrosoftSQLServer.model.SchemaDataMSSQL;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableCreations {

    private List<SchemaDataMSSQL> schemaTable;
    private String origin;
}
