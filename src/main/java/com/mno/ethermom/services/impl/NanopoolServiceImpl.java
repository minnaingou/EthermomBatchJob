package com.mno.ethermom.services.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mno.ethermom.models.PoolStats;
import com.mno.ethermom.models.ethermine.CurrentStatsJsonResponse;
import com.mno.ethermom.models.ethermine.Worker;
import com.mno.ethermom.models.ethermine.WorkersJsonResponse;
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

public class EthermineServiceImpl implements PoolService {

    private static final String STATUS_OK = "OK";

    private static final String API_URL = "https://api.ethermine.org/miner";
    private static final String API_FUNCTION_CURRENTSTATS = "currentStats";
    private static final String API_FUNCTION_WORKERS = "workers";

    @Override
    public PoolStats getPoolStats(String walletAddr) throws Exception {

        PoolStats poolStats = new PoolStats();

        CurrentStatsJsonResponse currentStatsRes = (CurrentStatsJsonResponse) this.getJsonFromUrl(
                this.getApiUrl(walletAddr, API_FUNCTION_CURRENTSTATS),
                CurrentStatsJsonResponse.class);

        if (currentStatsRes == null || !STATUS_OK.equals(currentStatsRes.getStatus())) {
            throw new Exception("API is not working.");
        }

        double totalExpectedHash = ConfigUtil.getExpectedHash(null);
        if (totalExpectedHash <= 0) {
            throw new IllegalArgumentException("Invalid expected hashrate. Please set 'expectedHash' property");
        }

        if (currentStatsRes.getData().getReportedHashrate() < totalExpectedHash) {

            WorkersJsonResponse workersRes = (WorkersJsonResponse) this.getJsonFromUrl(
                    this.getApiUrl(walletAddr, API_FUNCTION_WORKERS), WorkersJsonResponse.class);

            if (workersRes == null || !STATUS_OK.equals(workersRes.getStatus())) {
                throw new Exception("API is not working.");
            }

            List<Worker> workers = workersRes.getData();
            if (workers == null || workers.isEmpty()) {
                throw new IllegalArgumentException("No active workers");
            }

            Map<String, Double> problemWorkers = new LinkedHashMap<>();
            for (Worker worker : workers) {
                double expectedHash = ConfigUtil.getExpectedHash(worker.getWorker());
                if (expectedHash <= 0) {
                    throw new IllegalArgumentException(
                            "Invalid expected hashrate. Please set workers 'expectedHash' property");
                }

                if (worker.getReportedHashrate() == null) {
                    problemWorkers.put(worker.getWorker(), -1.0);
                    continue;
                }

                if (worker.getReportedHashrate() < expectedHash) {
                    problemWorkers.put(worker.getWorker(),
                            ConversionUtil.convertToMHs(worker.getReportedHashrate()));
                }
            }

            poolStats.setProblemWorkers(problemWorkers);
        }

        poolStats.getShare().setSupported(true);
        poolStats.getShare().setValid(currentStatsRes.getData().getValidShares());
        poolStats.getShare().setStale(currentStatsRes.getData().getStaleShares());

        return poolStats;
    }

    public Object getJsonFromUrl(String restUrl, Class<?> clazz) throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(restUrl)
                .build(); // defaults to GET

        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            Gson jsonRes = new GsonBuilder().create();
            return jsonRes.fromJson(response.body().string(), clazz);
        }

        return null;
    }

    public String getApiUrl(String walletAddr, String apiFunction) {
        return API_URL + (API_URL.endsWith("/") ? "" : "/") + walletAddr + "/" + apiFunction;
    }

}