package com.mno.ethermom.utils;

import com.mno.ethermom.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigUtil {

	private static final Map<String, String> propsMap = new HashMap<>();

	private static final String CONFIG_PATH = "ethermom.properties";

	// config properties
	private static final String CONFIG_WALLET = "wallet";
	private static final String CONFIG_ENABLED = "enabled";
	private static final String CONFIG_POOL = "pool";
	private static final String CONFIG_CONTINUOUS = "continuousReport";
	private static final String CONFIG_STALECHECK = "checkStale";
	private static final String CONFIG_STALETOLERANCE = "staleTolerance";
	private static final String CONFIG_EXPECTED_HASH = "expectedHash";
	private static final String CONFIG_WORKERS = "workers";
	private static final String CONFIG_TELEGRAM_ENABLED = "telegram.enabled";
	private static final String CONFIG_TELEGRAM_API_KEY = "telegram.apiKey";
	private static final String CONFIG_TELEGRAM_CHATID = "telegram.chatId";
	private static final String CONFIG_IFTTT_ENABLED = "ifttt.webhook.enabled";
	private static final String CONFIG_IFTTT_API_KEY = "ifttt.webhook.apiKey";
	private static final String CONFIG_IFTTT_EVENTNAME = "ifttt.webhook.eventName";

	public static void loadConfigs() throws IOException, URISyntaxException {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			String rootDir = new File(
					App.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
			input = new FileInputStream(rootDir + "/" + CONFIG_PATH);
			System.out.println("Reading config from " + rootDir + "/" + CONFIG_PATH);
			
			prop.load(input);
			
			for (String key : prop.stringPropertyNames()) {
				propsMap.put(key, prop.getProperty(key));
			}
			
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static boolean isEnabled() {
		return propsMap.getOrDefault(CONFIG_ENABLED, "2").equals("1");
	}

	public static String getWalletAddress() {
		return propsMap.get(CONFIG_WALLET);
	}

	public static String getPool() {
		return propsMap.get(CONFIG_POOL);
	}

	public static boolean isContinuousReport() {
		return Boolean.parseBoolean(propsMap.getOrDefault(CONFIG_CONTINUOUS, "true"));
	}

	public static boolean isStaleCheck() {
		return Boolean.parseBoolean(propsMap.getOrDefault(CONFIG_STALECHECK, "true"));
	}

	public static double getStaleTolerance() {
		return Double.parseDouble(propsMap.get(CONFIG_STALETOLERANCE));
	}

	public static double getExpectedHash(String forWorker) {
		String expectedHashStr;
		if (forWorker == null) {
			expectedHashStr = propsMap.get(CONFIG_EXPECTED_HASH);
		} else {
			expectedHashStr = propsMap.get(CONFIG_WORKERS + "." + forWorker + "." + CONFIG_EXPECTED_HASH);
		}
		if (expectedHashStr != null) {
			double expectedHash = 0;
			try {
				return Double.parseDouble(expectedHashStr);
			} catch (NumberFormatException e) {
				return 0;
			}
			
		} else {
			return -1;
		}
	}
	
	public static boolean isTelegramEnabled() {
		return propsMap.getOrDefault(CONFIG_TELEGRAM_ENABLED, "2").equals("1");
	}
	
	public static String getTelegramApiKey() {
		return propsMap.get(CONFIG_TELEGRAM_API_KEY);
	}
	
	public static String getTelegramChatId() {
		String chatId = propsMap.get(CONFIG_TELEGRAM_CHATID);
		if (chatId != null && !chatId.isEmpty()) {
			if (!chatId.startsWith("@")) chatId = "@" + chatId;
			return chatId;
		}
		return null;
	}
	
	public static boolean isIFTTTEnabled() {
		return propsMap.getOrDefault(CONFIG_IFTTT_ENABLED, "2").equals("1");
	}
	
	public static String getIFTTTApiKey() {
		return propsMap.get(CONFIG_IFTTT_API_KEY);
	}
	
	public static String getIFTTTEventName() {
		return propsMap.get(CONFIG_IFTTT_EVENTNAME);
	}

}
