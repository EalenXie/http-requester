package com.github.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

/**
 * Created by EalenXie on 2021/8/4 9:24
 * 声明一个示例处理器, 例如本例将收集信息打印
 */
@Component
@EnableAsync
public class CustomReqInfoCollector implements ReqInfoCollector {
    private static final Logger log = LoggerFactory.getLogger(CustomReqInfoCollector.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    @Override
    public void collect(ReqInfo reqInfo) {
        try {
            // 这里自行选择收集方式,可选择写入队列或文件或数据库,本例是直接处理成json日志打印
            log.info(objectMapper.writeValueAsString(reqInfo));
        } catch (JsonProcessingException e) {
            log.error("parse reqInfo error ", e);
        }
    }
}
