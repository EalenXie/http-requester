package com.github.http;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

class HttpClientProxyTest {


    public static void main(String[] args) {
        ReqInfoCollector collector = System.out::println;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpClientProxy httpClientProxy = new HttpClientProxy(httpClient, collector);
        // 和HttpClient使用方式一模一样
        //2.创建post请求方式实例
        HttpPost httpPost = new HttpPost("http://localhost:7877/sayTestJson?k=1");
        //2.1设置请求头 发送的是json数据格式
        httpPost.setHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Connection", "Close");
        String jsonStr = " {\"username\":\"aries\",\"password\":\"666666\"}";
        StringEntity entity = new StringEntity(jsonStr, StandardCharsets.UTF_8);
        entity.setContentEncoding("UTF-8");  //设置编码格式
        // 发送Json格式的数据请求
        entity.setContentType("application/json");
        //把请求消息实体塞进去
        httpPost.setEntity(entity);
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClientProxy.execute(httpPost);
            //封装成字符流来输出
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println("------------------" + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpResponse != null) {
                try {
                    ((CloseableHttpResponse) httpResponse).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}