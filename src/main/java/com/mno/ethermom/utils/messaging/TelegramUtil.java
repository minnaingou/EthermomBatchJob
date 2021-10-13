package com.mno.ethermom.utils.messaging;

import com.mno.ethermom.utils.ConfigUtil;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class TelegramUtil {
	
	private static final String API_URL = "https://api.telegram.org/bot";
	private static final String API_FUNCTION_SENDMSG = "sendMessage";

	public static void sendMessage(String message) throws Exception {
		String apiKey = ConfigUtil.getTelegramApiKey();
		if (apiKey == null || apiKey.isEmpty()) {
			throw new IllegalArgumentException("Telegram API Key is missing.");
		}
		
		String chatId = ConfigUtil.getTelegramChatId();
		if (chatId == null || chatId.isEmpty()) {
			throw new IllegalArgumentException("Telegram channel chat Id is missing.");
		}

		OkHttpClient client = new OkHttpClient();

		StringBuilder restUrl = new StringBuilder();
		restUrl.append(API_URL);
		restUrl.append(apiKey);
		restUrl.append("/" + API_FUNCTION_SENDMSG);
		restUrl.append("?chat_id=").append(chatId);
		restUrl.append("&parse_mode=HTML");
		restUrl.append("&text=").append(message);

		Request request = new Request.Builder()
				.url(restUrl.toString())
				.build(); // defaults to GET

		Response response = client.newCall(request).execute();
		if (! response.isSuccessful()) {
			throw new Exception("Failed to send message with Telegram.");
		}
	}

}
