package com.baiyun.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
public class Client implements Serializable {

    private Long id;
    private String clientName;
    private String clientId;
    private String clientSecret;


}
