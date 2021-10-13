package com.mno.ethermom.models;

import lombok.Data;

import java.util.Map;

@Data
public class PoolStats {

    private Map<String, Double> problemWorkers;
    private Share share;

    public PoolStats() {
        this.share = new Share();
        share.setSupported(false);
    }

}