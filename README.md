Http Requester
==================

#### 获取调用第三方接口请求信息的工具包

主要采用设计模式之`代理模式`实现，<b>不改变原对象请求的实现方式</b>,通过对应的代理类的<b>收集器</b>来实现对请求信息进行获取。


主要支持的几大主流的请求`Proxy`类:

- `HttpClientProxy`: 支持获取apache `HttpClient` 请求信息的代理类
- `OkHttpClientProxy`: 支持获取 `OkHttpClient` 请求信息的代理类
- `RestTemplateProxy`: 支持获取 `RestTemplate` 请求信息的代理类


主要使用方式 :

##### 1. 实现请求信息收集的接口`ReqInfoCollector`

对收集到的请求数据自行处理方式(产生日志,或写入文件,或写入数据库). 提示: 收集逻辑最好异步处理 。示例如下 :

```java

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

/**
 * 声明一个示例处理器, 例如本例将请求信息处理成json日志打印
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
			// 这里自行选择处理方式,可选择写入队列或文件或数据库,本例是直接处理成json日志打印
            log.info(objectMapper.writeValueAsString(reqInfo));
        } catch (JsonProcessingException e) {
            log.error("parse reqInfo error ", e);
        }
    }
}

```

##### 2. 初始化Proxy对象,对应Proxy传入对应的原对象进行初始化，例如初始化`RestTemplateProxy`
```
	 // 收集器实现
	 CustomReqInfoCollector collector= new CustomReqInfoCollector();
	 // 自己创建的RestTemplate
	 RestTemplate restTemplate = new RestTemplate();
	 // 入参为自己创建的RestTemplate对象, 以及请求信息收集的收集器实现
     RestTemplateProxy restTemplateProxy = new RestTemplateProxy(restTemplate, collector);
```

##### 3. 调用`Proxy`对象发起请求,例如`RestTemplateProxy`

基本和原对象`RestTemplate`调用方式一模一样。示例如下 :

```
	RestTemplateProxy restTemplateProxy = new RestTemplateProxy(restTemplate, collector);
	// 设置appName 最佳实践获取spring.application.name
	restTemplateProxy.setAppName("demo");
	// 请求头 application/json
	HttpHeaders httpHeaders = new HttpHeaders();
	httpHeaders.setContentType(MediaType.APPLICATION_JSON);
	// 请求body
	ObjectMapper objectMapper = new ObjectMapper();
	ObjectNode json = objectMapper.createObjectNode();
	json.put("zhang", 123);
	HttpEntity<String> req = new HttpEntity<>(json.toString(), httpHeaders);
	// restTemplateProxy发起请求
	String result = restTemplateProxy.postForObject("http://localhost:7877/sayTestJson?k=1", req, String.class);

```

##### 4.本例中，收集器效果如下:
```
11:06:35.064 [main] INFO com.github.http.CustomReqInfoCollector - {"appName":"demo","host":"localhost","port":7877,"url":"http://localhost:7877/sayTestJson?k=1","httpHeaders":{"Content-Type":["application/json"]},"method":"POST","body":"{\"zhang\":123}","timestamp":1628046394981,"resp":"{\"s\":\"111\"}","statusCode":200,"costTime":39,"remarks":"com.github.http.RestTemplateProxy Exchange Success !","urlParam":"k=1"}
```

#### 请求信息对象ReqInfo属性说明

| 字段 | 类型  | 注释 |
| :------- | :------------ | :------------------------------ | 
| appName | String | 应用名称|
| host | String | 主机  |
| port | int | 端口号  |
| url   | String  | 请求url |
| httpHeaders  | Object |请求的http header信息|
| method  | String  |请求的http method  | 
| body | Object  | 请求体 | 
| timestamp | long | 请求时间戳 |
| resp  | Object | 请求响应体  | 
| statusCode| int | http状态码 |
| costTime | int | 请求耗时|
| remarks | String | 请求备注信息  |
| success  | boolean | 请求状态 异常 false 正常 true |
| urlParam  | String | url路径参数|
