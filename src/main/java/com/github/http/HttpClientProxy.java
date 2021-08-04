package com.github.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by EalenXie on 2021/7/14 15:39
 */
public class HttpClientProxy {

    private final Log log = LogFactory.getLog(getClass());

    private final HttpClient httpClient;

    private final String className = getClass().getName();

    private String appName;

    private final ReqInfoCollector collector;

    public HttpClientProxy(HttpClient httpClient, ReqInfoCollector collector) {
        this.httpClient = httpClient;
        this.collector = collector;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public HttpResponse execute(HttpUriRequest request) throws IOException {
        return execute(determineTarget(request), request);
    }

    public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException {
        return execute(determineTarget(request), request, context);
    }

    public HttpResponse execute(HttpHost host, HttpRequest request) throws IOException {
        return execute(host, request, (HttpContext) null);
    }

    public HttpResponse execute(HttpHost host, HttpRequest request, HttpContext context) throws IOException {
        return execute(host, request, context, collector);

    }

    private static HttpHost determineTarget(final HttpUriRequest request) throws ClientProtocolException {
        HttpHost target = null;
        final URI requestURI = request.getURI();
        if (requestURI.isAbsolute()) {
            target = URIUtils.extractHost(requestURI);
            if (target == null) {
                throw new ClientProtocolException("URI does not specify a valid host name: " + requestURI);
            }
        }
        return target;
    }

    private HttpResponse execute(HttpHost host, HttpRequest request, HttpContext context, ReqInfoCollector collector) throws IOException {
        HttpResponse response;
        long timestamp = System.currentTimeMillis();
        boolean success = false;
        Object body = null;
        Object resp = null;
        String remarks = null;
        int costTime = -2;
        int rawStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        try {
            response = httpClient.execute(host, request, context);
            costTime = (int) (System.currentTimeMillis() - timestamp);
            if (request instanceof HttpEntityEnclosingRequestBase) {
                body = EntityUtils.toString(((HttpEntityEnclosingRequestBase) request).getEntity());
            }
            success = true;
            if (response != null) {
                rawStatusCode = response.getStatusLine().getStatusCode();
                resp = getStringByHttpResponse(response);
            }
            remarks = String.format("%s Execute Success !", className);
        } catch (HttpResponseException e) {
            rawStatusCode = e.getStatusCode();
            remarks = String.format("%s Execute Fail. %s", className, e.getMessage());
            throw e;
        } catch (Exception e) {
            remarks = String.format("%s Execute Fail. %s", className, e.getMessage());
            throw e;
        } finally {
            ReqInfo info = new ReqInfo();
            info.setCostTime((int) (costTime == -2 ? (System.currentTimeMillis() - timestamp) : costTime));
            info.setAppName(getAppName());
            info.setHost(host.getHostName());
            info.setPort(host.getPort());
            info.setUrl(request.getRequestLine().getUri());
            info.setHttpHeaders(request.getAllHeaders() != null ? Arrays.toString(request.getAllHeaders()) : null);
            info.setMethod(request.getRequestLine().getMethod());
            info.setBody(body);
            info.setTimestamp(timestamp);
            info.setResp(resp);
            info.setStatusCode(rawStatusCode);
            info.setSuccess(success);
            info.setRemarks(remarks);
            if (request instanceof HttpUriRequest) {
                info.setUrlParam(((HttpUriRequest) request).getURI().getQuery());
            }
            collector.collect(info);
        }
        return response;
    }

    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> handler) throws IOException {
        return execute(determineTarget(request), request, handler, null);

    }

    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> handler, HttpContext context) throws IOException {
        return execute(determineTarget(request), request, handler, context);
    }

    public <T> T execute(HttpHost host, HttpRequest request, ResponseHandler<? extends T> handler) throws IOException {
        return execute(host, request, handler, null);
    }

    public <T> T execute(HttpHost host, HttpRequest request, ResponseHandler<? extends T> handler, HttpContext context) throws IOException {
        Args.notNull(handler, "Response handler");
        HttpResponse response = execute(host, request, context);
        try {
            final T result = handler.handleResponse(response);
            final HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
            return result;
        } catch (final ClientProtocolException t) {
            final HttpEntity entity = response.getEntity();
            try {
                EntityUtils.consume(entity);
            } catch (final Exception t2) {
                // Log this exception. The original exception is more
                // important and will be thrown to the caller.
                log.warn("Error consuming content after an exception.", t2);
            }
            throw t;
        } finally {
            if (response instanceof CloseableHttpResponse) {
                ((CloseableHttpResponse) response).close();
            }
        }
    }


    private String getStringByHttpResponse(HttpResponse response) {
        HttpEntity sourceEntity = response.getEntity();
        if (sourceEntity == null) return null;
        final ByteArrayBuffer buffer = new ByteArrayBuffer(4096);
        byte[] bytes;
        try (InputStream content = sourceEntity.getContent()) {
            final byte[] tmp = new byte[4096];
            int l;
            while ((l = content.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            bytes = buffer.toByteArray();
            BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
            basicHttpEntity.setChunked(sourceEntity.isChunked());
            basicHttpEntity.setContentEncoding(sourceEntity.getContentEncoding());
            basicHttpEntity.setContentType(sourceEntity.getContentType());
            basicHttpEntity.setContentLength(bytes.length);
            basicHttpEntity.setContent(new ByteArrayInputStream(bytes));
            response.setEntity(basicHttpEntity);
            buffer.clear();
            EntityUtils.consumeQuietly(sourceEntity);
        }
        Header contentEncoding = sourceEntity.getContentEncoding();
        Charset charset;
        try {
            if (contentEncoding != null && contentEncoding.getValue() != null && contentEncoding.getValue().length() > 0) {
                charset = Charset.forName(contentEncoding.getValue());
            } else {
                charset = StandardCharsets.UTF_8;
            }
        } catch (Exception e) {
            log.warn("Content Encoding Error", e);
            charset = StandardCharsets.UTF_8;
        }
        String result = new String(bytes, charset);
        EntityUtils.consumeQuietly(sourceEntity);
        return result;
    }


}
