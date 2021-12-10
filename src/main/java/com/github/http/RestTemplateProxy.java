package com.github.http;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.*;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Created by EalenXie on 2021/7/14 15:32
 *
 * TODO
 */
public class RestTemplateProxy implements RestOperations {
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

    @Override
    public <T> T getForObject(String url, Class<T> responseType, Map<String, ?> uriVariables) {
        return getForEntity(url, responseType, uriVariables).getBody();
    }

    @Override
    public <T> T getForObject(URI url, Class<T> responseType) throws RestClientException {
        return null;
    }

    @Override
    public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) {
        return getForEntity(url, responseType, uriVariables).getBody();
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Map<String, ?> uriVariables) {
        return exchange(url, HttpMethod.GET, null, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(URI url, Class<T> responseType) throws RestClientException {
        return null;
    }

    @Override
    public HttpHeaders headForHeaders(String url, Object... uriVariables) throws RestClientException {
        return null;
    }

    @Override
    public HttpHeaders headForHeaders(String url, Map<String, ?> uriVariables) throws RestClientException {
        return null;
    }

    @Override
    public HttpHeaders headForHeaders(URI url) throws RestClientException {
        return null;
    }

    @Override
    public URI postForLocation(String url, Object request, Object... uriVariables) throws RestClientException {
        return null;
    }

    @Override
    public URI postForLocation(String url, Object request, Map<String, ?> uriVariables) throws RestClientException {
        return null;
    }

    @Override
    public URI postForLocation(URI url, Object request) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables) {
        return exchange(url, HttpMethod.GET, null, responseType, uriVariables);
    }

    @Override
    public <T> T postForObject(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) {
        return postForEntity(url, request, responseType, uriVariables).getBody();
    }

    @Override
    public <T> T postForObject(URI url, Object request, Class<T> responseType) throws RestClientException {
        return null;
    }

    @Override
    public <T> T postForObject(String url, Object request, Class<T> responseType, Object... uriVariables) {
        return postForEntity(url, request, responseType, uriVariables).getBody();
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) {
        return exchange(url, HttpMethod.POST, httpEntity(request), responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) {
        return exchange(url, HttpMethod.POST, httpEntity(request), responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(URI url, Object request, Class<T> responseType) throws RestClientException {
        return exchange(url, HttpMethod.POST, httpEntity(request), responseType);
    }

    @Override
    public void put(String url, Object request, Object... uriVariables) throws RestClientException {

    }

    @Override
    public void put(String url, Object request, Map<String, ?> uriVariables) throws RestClientException {

    }

    @Override
    public void put(URI url, Object request) throws RestClientException {

    }

    @Override
    public <T> T patchForObject(String url, Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return null;
    }

    @Override
    public <T> T patchForObject(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return null;
    }

    @Override
    public <T> T patchForObject(URI url, Object request, Class<T> responseType) throws RestClientException {
        return null;
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

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return exchange(uriTemplateHandler.expand(url, uriVariables), method, requestEntity, responseType, collector);
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

    @Override
    public Set<HttpMethod> optionsForAllow(String url, Object... uriVariables) throws RestClientException {
        return restTemplate.optionsForAllow(url, uriVariables);
    }

    @Override
    public Set<HttpMethod> optionsForAllow(String url, Map<String, ?> uriVariables) throws RestClientException {
        return restTemplate.optionsForAllow(url, uriVariables);
    }

    @Override
    public Set<HttpMethod> optionsForAllow(URI url) throws RestClientException {
        return restTemplate.optionsForAllow(url);
    }

    @Override
    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) {
        return exchange(url, method, requestEntity, responseType, collector);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Object... uriVariables) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, Class<T> responseType) throws RestClientException {
        return null;
    }

    @Override
    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return null;
    }

    @Override
    public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Object... uriVariables) throws RestClientException {
        return execute(uriTemplateHandler.expand(url, uriVariables), method, requestCallback, responseExtractor);
    }

    @Override
    public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Map<String, ?> uriVariables) throws RestClientException {
        return execute(uriTemplateHandler.expand(url, uriVariables), method, requestCallback, responseExtractor);
    }

    @Override
    public <T> T execute(URI url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) throws RestClientException {
        ClientHttpResponse response = null;
        try {
            ClientHttpRequest request = restTemplate.getRequestFactory().createRequest(url, method);
            restTemplate.getClientHttpRequestInitializers().forEach(initializer -> initializer.initialize(request));
            if (requestCallback != null) {
                requestCallback.doWithRequest(request);
            }
            response = request.execute();
            ResponseErrorHandler errorHandler = restTemplate.getErrorHandler();
            boolean hasError = errorHandler.hasError(response);
            if (hasError) {
                errorHandler.handleError(url, method, response);
            }
            return (responseExtractor != null ? responseExtractor.extractData(response) : null);
        } catch (IOException ex) {
            String resource = url.toString();
            String query = url.getRawQuery();
            resource = (query != null ? resource.substring(0, resource.indexOf('?')) : resource);
            throw new ResourceAccessException("I/O error on " + method.name() +
                    " request for \"" + resource + "\": " + ex.getMessage(), ex);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, ReqInfoCollector collector) {
        if (collector == null) {
            return restTemplate.exchange(url, method, requestEntity, responseType);
        }
        ResponseEntity<T> responseEntity;
        boolean success = false;
        Object body = null;
        Object resp = null;
        String remarks = null;
        Object httpHeaders = null;
        int rawStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        long timestamp = System.currentTimeMillis();
        int costTime = -3;
        try {
            // 请求参数解析
            if (requestEntity != null) {
                body = requestEntity.getBody();
                httpHeaders = requestEntity.getHeaders();
            }
            responseEntity = restTemplate.exchange(url, method, requestEntity, responseType);
            costTime = (int) (System.currentTimeMillis() - timestamp);
            // 请求响应解析
            resp = responseEntity.getBody();
            // 请求标识
            success = true;
            rawStatusCode = responseEntity.getStatusCodeValue();
            remarks = String.format("%s Exchange Success !", className);
        } catch (RestClientResponseException e) {
            remarks = String.format("%s Exchange Fail. %s", className, e.getMessage());
            resp = e.getResponseBodyAsString();
            rawStatusCode = e.getRawStatusCode();
            throw e;
        } catch (Exception e) {
            remarks = String.format("%s Exchange Fail. %s", className, e.getMessage());
            throw e;
        } finally {
            ReqInfo info = new ReqInfo();
            info.setAppName(getAppName());
            info.setHost(url.getHost());
            info.setPort(url.getPort());
            info.setUrl(url.toString());
            info.setHttpHeaders(httpHeaders);
            info.setMethod(method.name());
            info.setBody(body);
            info.setTimestamp(timestamp);
            info.setCostTime(costTime == -3 ? (int) (System.currentTimeMillis() - timestamp) : costTime);
            info.setResp(resp);
            info.setStatusCode(rawStatusCode);
            info.setSuccess(success);
            info.setUrlParam(url.getQuery());
            info.setRemarks(remarks);
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
