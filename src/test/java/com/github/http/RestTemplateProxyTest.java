package com.github.http;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.http.ReqInfoCollector;
import com.github.http.RestTemplateProxy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

class RestTemplateProxyTest {




    public static void main(String[] args) {


        ReqInfoCollector collector = System.out::println;

        RestTemplate restTemplate = new RestTemplate();
        RestTemplateProxy restTemplateProxy = new RestTemplateProxy(restTemplate, collector);

        HttpHeaders httpHeaders= new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode json = objectMapper.createObjectNode();
        json.put("zhang", 123);
        //用HttpEntity封装整个请求报文
        HttpEntity<String> req = new HttpEntity<>(json.toString(), httpHeaders);

        //服务端返回的json格式："
        String result = restTemplateProxy.postForObject("http://localhost:7877/sayTestJson?k=1", req, String.class);

    }

}