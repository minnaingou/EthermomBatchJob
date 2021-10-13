package com.mno.ethermom;

import com.mno.ethermom.models.PoolStats;
import com.mno.ethermom.services.PoolService;
import com.mno.ethermom.services.impl.EthermineServiceImpl;
import com.mno.ethermom.services.impl.NanopoolServiceImpl;
import com.mno.ethermom.utils.ConfigUtil;
import com.mno.ethermom.utils.messaging.MessagingUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class App {

    private static final File lockfile = new File("reported.lock");
    private static final File runningfile = new File("running.lock");

    public static void main(String[] args) {

        System.out.println("Job started.");

        try {
            ConfigUtil.loadConfigs();

            if (!ConfigUtil.isEnabled()) {
                System.exit(0);
            }

            String walletAddr = ConfigUtil.getWalletAddress();
            if (walletAddr == null || walletAddr.isEmpty()) {
                throw new IllegalArgumentException("Wallet not set.");
            }
            walletAddr = walletAddr.startsWith("0x") ? walletAddr : "0x".concat(walletAddr);

            PoolService poolService;

            switch (ConfigUtil.getPool()) {
                case "ethermine":
                    poolService = new EthermineServiceImpl();
                    break;
                case "nanopool":
                    poolService = new NanopoolServiceImpl();
                    break;
                default:
                    throw new Exception("Pool not supported");
            }

            PoolStats poolStats = poolService.getPoolStats(walletAddr);

            if (poolStats.getProblemWorkers() != null && !poolStats.getProblemWorkers().isEmpty()) {
                manageReport(poolStats.getProblemWorkers());
            } else if (lockfile.exists()) {
                lockfile.delete();
                MessagingUtil.sendMessage("Previously failed workers are back to normal.");
            }

            if (ConfigUtil.isStaleCheck() && poolStats.getShare().isSupported()) {
                if (poolStats.getShare().getStale() > (poolStats.getShare().getValid() * ConfigUtil.getStaleTolerance() / 100)) {
                    MessagingUtil.sendMessage("Stale shares are too high @ " +
                            poolStats.getShare().getStale() + "/" +
                            poolStats.getShare().getValid()
                            + ". Check your workers.%0A%0A<i>Stale Tolerance is set to " + ConfigUtil.getStaleTolerance() + "%</i>");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            runningfile.delete();
            try {
                runningfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Job ended.");
            System.exit(0);
        }
    }

    private static void manageReport(Map<String, Double> problemWorkers) throws Exception {

        boolean reportFlag = true;

        if (lockfile.exists()) {
            lockfile.delete();
            reportFlag = ConfigUtil.isContinuousReport();
        }
        lockfile.createNewFile();

        if (reportFlag) {
            StringBuilder message = new StringBuilder();
            message.append("Reported hashrate is lower than expected for following worker(s).");
            for (String key : problemWorkers.keySet()) {
                if (problemWorkers.get(key) < 0) {
                    message.append("%0A").append(key).append(" is offline");
                } else {
                    message.append("%0A").append(key).append(" @").append(problemWorkers.get(key)).append("MH/s");
                }
            }

            MessagingUtil.sendMessage(message.toString());
        }
    }

}
