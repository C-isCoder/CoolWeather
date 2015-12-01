package com.iscoderweather.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class HttpUtil {
		public static final String APIKEY = "71f90380832b91e07e8f97272ceecea6";
		public static void sendHttpRequest (final String address, final HttpCallbackListener listener) {
			new Thread(new Runnable(){
				@Override
				public void run() {
					HttpURLConnection connection = null;
					try {
						URL url = new URL(address);
						connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("GET");
						//connection.setRequestProperty("apikey", APIKEY);
						connection.setReadTimeout(8000);
						connection.setConnectTimeout(8000);
						InputStream in = connection.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(in,"utf-8"));
						StringBuilder response = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							response.append(line);
						}
						Log.d("cxd", response.toString());
						if (listener != null) {
							//回调onFinish()方法
							listener.onFinish(response.toString());
						}
						
					} catch (Exception e){
						if (listener != null) {
							//回调onError()方法
							listener.onError(e);
						}
					} finally {
						if (connection != null) {
							connection.disconnect();
						}
					}
				}
				
			}).start();
		}
}
