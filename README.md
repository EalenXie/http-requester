Http Requester
==================

#### 获取调用第三方接口请求信息的工具包

不改变原有的实现方式,通过对应的代理类来实现对请求信息进行获取。



主要支持的`Proxy`类: 

- HttpClientProxy: 支持获取apache `HttpClient` 请求信息的代理类
- OkHttpClientProxy: 支持获取 `OkHttpClient` 请求信息的代理类
- RestTemplateProxy: 支持获取 `RestTemplate` 请求信息的代理类


主要使用方式 : 

1. 实现 请求信息收集的接口 `ReqInfoCollector` , 对收集到的请求数据自行处理(产生日志,或写入文件,或写入数据库). 提示: 收集逻辑最好异步处理 

2. 初始化Proxy对象,对应Proxy传入对应的原对象进行初始化

3. 调用Proxy对象发起请求,基本和原对象调用方式一模一样


例如HttpClientProxy,示例如下 : 


```
    // 1.实现请求信息收集ReqInfoCollector (这个例子只是打印对象) 
    ReqInfoCollector collector = System.out::println;

    // 2.初始化HttpClientProxy
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpClientProxy httpClientProxy = new HttpClientProxy(httpClient, collector);

    // 3. 调用Proxy 和HttpClient使用方式一模一样
    HttpResponse httpResponse = httpClientProxy.execute(httpPost);

```

收集的请求信息类 `ReqInfo`:
```java

public class ReqInfo {

    /**
     * 应用名称
     */
    private String appName;
    /**
     * 请求host
     */
    private String host;
    /**
     * 请求port
     */
    private int port;
    /**
     * 请求url
     */
    private String url;
    /**
     * 请求header信息
     */
    private Object httpHeaders;
    /**
     * 请求method
     */
    private String method;
    /**
     * 请求参数
     */
    private Object req;
    /**
     * 请求时间
     */
    private long timestamp;
    /**
     * 响应参数
     */
    private Object resp;
    /**
     * http 返回码
     */
    private int statusCode;
    /**
     * 耗时时间
     */
    private long costTime;
    /**
     * 备注
     */
    private String desc;
    /**
     * 请求状态 异常 false 正常 true
     */
    private boolean success;
    /**
     * url路径参数
     */
    private String urlParam;
    
    // 省略getter setter .....
}

```

