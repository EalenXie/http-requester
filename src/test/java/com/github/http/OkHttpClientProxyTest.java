package com.github.http;

import okhttp3.*;

import java.io.IOException;

/**
 * Created by EalenXie on 2021/7/15 17:10
 */
public class OkHttpClientProxyTest {


    public static void main(String[] args) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        ReqInfoCollector collector = System.out::println;
        OkHttpClientProxy okHttpClientProxy = new OkHttpClientProxy(okHttpClient, collector);
        String url = "http://localhost:7877/sayTestJson?k=1";
        String json = "{\"zhang\":\"123\"}";
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();


        // 同步请求
        String s;
        try (Response response = okHttpClientProxy.newCallExecute(request)) {
            s = response.body().string();
            System.out.println("----------" + s);
        }

//        // 异步请求
//        okHttpClientProxy.newCallEnqueue(request, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String s = response.body().string();
//                System.out.println("----------" + s);
//                response.close();
//            }
//        });


    }
}
