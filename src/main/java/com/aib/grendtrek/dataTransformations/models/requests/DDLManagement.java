package com.aib.grendtrek.dataTransformations.models.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DDLManagement {

    private String DDL;
    private String TableName;

}
