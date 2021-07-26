package com.github.http;

import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

import java.net.URI;
import java.util.Map;

/**
 * Created by EalenXie on 2021/7/14 15:32
 */
public class RestTemplateProxy {
    private final RestTemplate restTemplate;
    private final UriTemplateHandler uriTemplateHandler = initUriTemplateHandler();
    private final String className = this.getClass().getName();
    private String appName;
    private final ReqInfoCollector collector;

    private static DefaultUriBuilderFactory initUriTemplateHandler() {
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory();
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);
        return uriFactory;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    private String getAppName() {
        return this.appName;
    }

    public RestTemplate getRestTemplate() {
        return this.restTemplate;
    }

    public RestTemplateProxy(ClientHttpRequestFactory factory, ReqInfoCollector collector) {
        this(new RestTemplate(factory), collector);
    }

    public RestTemplateProxy(RestTemplate restTemplate, ReqInfoCollector collector) {
        this.restTemplate = restTemplate;
        this.collector = collector;
    }

    public <T> T getForObject(String url, Class<T> responseType, Map<String, ?> uriVariables) {
        return getForEntity(url, responseType, uriVariables).getBody();
    }

    public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) {
        return getForEntity(url, responseType, uriVariables).getBody();
    }

    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Map<String, ?> uriVariables) {
        return exchange(url, HttpMethod.GET, null, responseType, uriVariables);
    }

    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables) {
        return exchange(url, HttpMethod.GET, null, responseType, uriVariables);
    }

    public <T> T postForObject(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) {
        return postForEntity(url, request, responseType, uriVariables).getBody();
    }

    public <T> T postForObject(String url, Object request, Class<T> responseType, Object... uriVariables) {
        return postForEntity(url, request, responseType, uriVariables).getBody();
    }

    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) {
        return exchange(url, HttpMethod.POST, httpEntity(request), responseType, uriVariables);
    }

    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) {
        return exchange(url, HttpMethod.POST, httpEntity(request), responseType, uriVariables);
    }

    public <T> T putForObject(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) {
        return putForEntity(url, request, responseType, uriVariables).getBody();
    }

    public <T> T putForObject(String url, Object request, Class<T> responseType, Object... uriVariables) {
        return putForEntity(url, request, responseType, uriVariables).getBody();
    }

    public <T> ResponseEntity<T> putForEntity(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) {
        return exchange(url, HttpMethod.PUT, httpEntity(request), responseType, uriVariables);
    }

    public <T> ResponseEntity<T> putForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) {
        return exchange(url, HttpMethod.PUT, httpEntity(request), responseType, uriVariables);
    }

    public <T> ResponseEntity<T> deleteForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) {
        return exchange(url, HttpMethod.DELETE, httpEntity(request), responseType, uriVariables);
    }

    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) {
        return exchange(uriTemplateHandler.expand(url, uriVariables), method, requestEntity, responseType);
    }

    public void delete(String url, Object... uriVariables) {
        exchange(uriTemplateHandler.expand(url, uriVariables), HttpMethod.DELETE, null, Void.class);
    }

    public void delete(String url, Map<String, ?> uriVariables) {
        exchange(uriTemplateHandler.expand(url, uriVariables), HttpMethod.DELETE, null, Void.class);
    }

    public void delete(URI url) {
        exchange(url, HttpMethod.DELETE, null, Void.class);
    }

    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) {
        return exchange(url, method, requestEntity, responseType, collector);
    }

    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, ReqInfoCollector collector) {
        if (collector == null) {
            return restTemplate.exchange(url, method, requestEntity, responseType);
        }
        ResponseEntity<T> responseEntity;
        boolean success = false;
        Object req = null;
        Object resp = null;
        String desc = null;
        Object httpHeaders = null;
        int rawStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        long timestamp = System.currentTimeMillis();
        int costTime = -3;
        try {
            // 请求参数解析
            if (requestEntity != null) {
                req = requestEntity.getBody();
                httpHeaders = requestEntity.getHeaders();
            }
            responseEntity = restTemplate.exchange(url, method, requestEntity, responseType);
            costTime = (int) (System.currentTimeMillis() - timestamp);
            // 请求响应解析
            resp = responseEntity.getBody();
            // 请求标识
            success = true;
            rawStatusCode = responseEntity.getStatusCodeValue();
            desc = String.format("%s Exchange Success !", className);
        } catch (RestClientResponseException e) {
            desc = String.format("%s Exchange Fail. %s", className, e.getMessage());
            resp = e.getResponseBodyAsString();
            rawStatusCode = e.getRawStatusCode();
            throw e;
        } catch (Exception e) {
            desc = String.format("%s Exchange Fail. %s", className, e.getMessage());
            throw e;
        } finally {
            ReqInfo info = new ReqInfo();
            info.setAppName(getAppName());
            info.setHost(url.getHost());
            info.setPort(url.getPort());
            info.setUrl(url.toString());
            info.setHttpHeaders(httpHeaders);
            info.setMethod(method.name());
            info.setReq(req);
            info.setTimestamp(timestamp);
            info.setCostTime(costTime == -3 ? System.currentTimeMillis() - timestamp : costTime);
            info.setResp(resp);
            info.setStatusCode(rawStatusCode);
            info.setSuccess(success);
            info.setUrlParam(url.getQuery());
            info.setDesc(desc);
            collector.collect(info);
        }
        return responseEntity;
    }


    @SuppressWarnings(value = "unchecked")
    public <T> HttpEntity<T> httpEntity(T requestBody) {
        if (requestBody instanceof HttpEntity) {
            return (HttpEntity<T>) requestBody;
        } else if (requestBody != null) {
            return new HttpEntity<>(requestBody);
        } else {
            return new HttpEntity<>(null, null);
        }
    }


}
