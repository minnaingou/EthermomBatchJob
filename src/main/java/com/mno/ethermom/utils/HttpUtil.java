package com.mno.ethermom.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpUtil {

	public static final String STATUS_OK = "OK";

	private static final String API_URL = "https://api.ethermine.org/miner";
	public static final String API_FUNCTION_CURRENTSTATS = "currentStats";
	public static final String API_FUNCTION_WORKERS = "workers";

	public static Object getJsonFromUrl(String restUrl, Class<?> clazz) throws IOException {

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

	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static String getApiUrl(String walletAddr, String apiFunction) {
		return API_URL + (API_URL.endsWith("/") ? "" : "/") + walletAddr + "/" + apiFunction;
	}

}
