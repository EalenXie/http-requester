package com.github.http;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

class RestTemplateProxyTest {


    public static void main(String[] args) {
        ReqInfoCollector collector = new ReqInfoCollector() {

            private final Logger log = LoggerFactory.getLogger(CustomReqInfoCollector.class);
            private final ObjectMapper objectMapper = new ObjectMapper();

            public void collect(ReqInfo reqInfo) {
                try {
                    // 这里自行选择收集方式,可选择写入队列或文件或数据库,本例是直接处理成json日志打印
                    log.info(objectMapper.writeValueAsString(reqInfo));
                } catch (JsonProcessingException e) {
                    log.error("parse reqInfo error ", e);
                }
            }
        };
        RestTemplate restTemplate = new RestTemplate();
        RestTemplateProxy restTemplateProxy = new RestTemplateProxy(restTemplate, collector);
        // 设置appName 最佳实践获取spring.application.name
        restTemplateProxy.setAppName("demo");
        HttpHeaders httpHeaders = new HttpHeaders();
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