package com.example.pureweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    //封装通过OkHttp发送请求语句
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
