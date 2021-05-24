package com.mno.ethermom.utils.messaging;

import com.mno.ethermom.utils.ConfigUtil;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.net.URLEncoder;

public class IFTTTUtil {

	private static final String API_URL = "https://maker.ifttt.com/trigger/%s/with/key";

	public static void sendMessage(String message) throws Exception {
		String apiKey = ConfigUtil.getIFTTTApiKey();
		if (apiKey == null || apiKey.isEmpty()) {
			throw new IllegalArgumentException("IFTTT API Key is missing.");
		}

		String eventName = ConfigUtil.getIFTTTEventName();
		if (eventName == null || eventName.isEmpty()) {
			throw new IllegalArgumentException("IFTTT event name is missing.");
		}
		
		message = message.replaceAll("\n", "<br>");

		OkHttpClient client = new OkHttpClient();

		StringBuilder restUrl = new StringBuilder();
		restUrl.append(String.format(API_URL, eventName));
		restUrl.append(API_URL.endsWith("/") ? "" : "/");
		restUrl.append(apiKey);
		restUrl.append("?value1=" + URLEncoder.encode(message, "UTF-8"));

		Request request = new Request.Builder()
				.url(restUrl.toString())
				.build(); // defaults to GET

		Response response = client.newCall(request).execute();
		if (! response.isSuccessful()) {
			throw new Exception("Failed to send message with IFTTT.");
		}
	}

}
