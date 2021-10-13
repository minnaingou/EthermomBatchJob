package com.mno.ethermom.services;

import com.mno.ethermom.models.PoolStats;

public interface PoolService {

    PoolStats getPoolStats(String wallet) throws Exception;

}
