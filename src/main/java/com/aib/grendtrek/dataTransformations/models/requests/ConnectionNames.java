package com.aib.grendtrek.dataTransformations.models.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionNames {

    private String origin;
    private String destiny;

}
