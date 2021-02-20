package com.baiyun.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Status implements Serializable {

    private int code;
    private String msg;


}
