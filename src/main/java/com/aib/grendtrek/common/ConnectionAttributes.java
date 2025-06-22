package com.aib.grendtrek.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class ConnectionAttributes {

    private String driver;
    private String host;
    private Integer port;
    private String user;
    private String password;
    private String databaseToConnect;


}
