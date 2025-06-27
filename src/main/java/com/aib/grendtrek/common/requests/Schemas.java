package com.aib.grendtrek.common.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Schemas {

    private String origin;
    private List<String> schemas;

}
