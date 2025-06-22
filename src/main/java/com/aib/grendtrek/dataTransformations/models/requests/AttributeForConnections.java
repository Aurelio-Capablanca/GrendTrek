package com.aib.grendtrek.dataTransformations.models.requests;

import com.aib.grendtrek.common.ConnectionAttributes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeForConnections {

    private String connectionName;
    private ConnectionAttributes attributes;
    private Boolean isOrigin;

}
