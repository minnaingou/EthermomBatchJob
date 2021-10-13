package com.mno.ethermom.services.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mno.ethermom.models.PoolStats;
import com.mno.ethermom.models.ethermine.CurrentStatsJsonResponse;
import com.mno.ethermom.models.ethermine.Worker;
import com.mno.ethermom.models.ethermine.WorkersJsonResponse;
import com.mno.ethermom.models.nanopool.LastReported;
import com.mno.ethermom.models.nanopool.WorkerStats;
import com.mno.ethermom.models.nanopool.WorkersLastReported;
import com.mno.ethermom.services.PoolService;
import com.mno.ethermom.utils.ConfigUtil;
import com.mno.ethermom.utils.ConversionUtil;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NanopoolServiceImpl implements PoolService {

    private static final String BASE_URL = "https://api.nanopool.org/v1/eth";
    private static final String API_FUNCTION_CURRENTSTATS = "/reportedhashrate/";
    private static final String API_FUNCTION_WORKERS = "/reportedhashrates/";

    @Override
    public PoolStats getPoolStats(String walletAddr) throws Exception {

        PoolStats poolStats = new PoolStats();

        double totalExpectedHash = ConfigUtil.getExpectedHash(null);
        if (totalExpectedHash <= 0) {
            throw new IllegalArgumentException("Invalid expected hashrate. Please set 'expectedHash' property");
        }

        double reportedHash = this.getCurrentStats(walletAddr);

        if (reportedHash < totalExpectedHash) {
            Map<String, Double> problemWorkers = new LinkedHashMap<>();

            List<WorkerStats> workers = this.getWorkerStats(walletAddr);
            for (WorkerStats worker : workers) {
                double expectedHash = ConfigUtil.getExpectedHash(worker.getWorker().toLowerCase());
                if (expectedHash <= 0) {
                    throw new IllegalArgumentException(
                            "Invalid expected hashrate. Please set workers 'expectedHash' property");
                }

                if (worker.getHashrate() < expectedHash) {
                    problemWorkers.put(worker.getWorker(), worker.getHashrate());
                }
            }
            poolStats.setProblemWorkers(problemWorkers);
        }

        return poolStats;
    }

    private double getCurrentStats(String walletAddr) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL + API_FUNCTION_CURRENTSTATS + walletAddr)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            LastReported lastReported = new Gson().fromJson(response.body().string(), LastReported.class);
            return lastReported.getData();
        }
        return -1;
    }

    private List<WorkerStats> getWorkerStats(String walletAddr) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL + API_FUNCTION_WORKERS + walletAddr)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            WorkersLastReported workers = new Gson().fromJson(response.body().string(), WorkersLastReported.class);
            return workers.getData();
        }
        return null;
    }

}