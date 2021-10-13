package com.mno.ethermom.models;

import lombok.Data;

@Data
public class Share {

    private boolean supported;
    private int stale;
    private int valid;

}