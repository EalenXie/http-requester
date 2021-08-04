package com.github.http;

import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by EalenXie on 2021/7/15 11:16
 */
public class OkHttpClientProxy {


    private final ReqInfoCollector collector;

    private final OkHttpClient okHttpClient;

    private final String className = this.getClass().getName();

    private final Map<Callback, CallbackProxy> callbackProxyMap = new HashMap<>();

    private String appName;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public OkHttpClientProxy(OkHttpClient okHttpClient, ReqInfoCollector collector) {
        this.collector = collector;
        this.okHttpClient = okHttpClient;
    }

    public CallbackProxy getCallbackProxy(Callback callback) {
        CallbackProxy callbackProxy = callbackProxyMap.get(callback);
        if (callbackProxy == null) {
            synchronized (this) {
                callbackProxy = new CallbackProxy(collector, callback);
                callbackProxyMap.put(callback, callbackProxy);
                if (callbackProxyMap.size() > 100) {
                    callbackProxyMap.clear();
                }
            }
        }
        callbackProxy.setTimestamp(System.currentTimeMillis());
        return callbackProxy;
    }

    public Response callExecute(Call call) throws IOException {
        Request request = call.request();
        Response response;
        long timestamp = System.currentTimeMillis();
        boolean success = false;
        Object body = null;
        Object resp = null;
        String remarks = null;
        int costTime = -1;
        int rawStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        try {
            body = getRequest(request.body());
            response = call.execute();
            rawStatusCode = response.code();
            resp = getResponse(response);
            if (response.isSuccessful()) {
                success = true;
            }
            timestamp = response.sentRequestAtMillis();
            costTime = (int) (response.receivedResponseAtMillis() - timestamp);
            remarks = String.format("%s Call.Execute Success !", className);
        } catch (Exception e) {
            remarks = String.format("%s Call.Execute Fail. %s", className, e.getMessage());
            throw e;
        } finally {
            ReqInfo info = getReqInfoByRequest(request);
            info.setBody(body);
            info.setTimestamp(timestamp);
            info.setResp(resp);
            info.setStatusCode(rawStatusCode);
            info.setCostTime((int) (costTime == -1 ? System.currentTimeMillis() - timestamp : costTime));
            info.setSuccess(success);
            info.setUrlParam(request.url().query());
            info.setRemarks(remarks);
            collector.collect(info);
        }
        return response;
    }

    public void callEnqueue(Call call, Callback responseCallback) {
        call.enqueue(getCallbackProxy(responseCallback));
    }

    public Response newCallExecute(Request request) throws IOException {
        return callExecute(okHttpClient.newCall(request));
    }


    public void newCallEnqueue(Request request, Callback responseCallback) {
        callEnqueue(okHttpClient.newCall(request), responseCallback);
    }

    private ReqInfo getReqInfoByRequest(Request request) {
        ReqInfo info = new ReqInfo();
        try {
            info.setAppName(getAppName());
            info.setHost(request.url().host());
            info.setPort(request.url().port());
            info.setUrl(request.url().url().toString());
            info.setHttpHeaders(request.headers());
            info.setMethod(request.method());
        } catch (Exception e) {
            // ig
        }
        return info;
    }


    class CallbackProxy implements Callback {

        private final ReqInfoCollector collector;
        private final Callback callback;
        private long timestamp;

        public CallbackProxy(ReqInfoCollector collector, Callback callback) {
            this.collector = collector;
            this.callback = callback;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            Request request = call.request();
            ReqInfo info = getReqInfoByRequest(request);
            try {
                info.setBody(getRequest(request.body()));
                info.setTimestamp(timestamp);
                info.setResp(null);
                info.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                info.setCostTime((int) (System.currentTimeMillis() - timestamp));
                info.setSuccess(false);
                info.setUrlParam(request.url().query());
                info.setRemarks(String.format("%s Call.Enqueue Fail. %s", className, e.getMessage()));
            } catch (Exception c) {
                // ig
            } finally {
                collector.collect(info);
            }
            callback.onFailure(call, e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            Request request = call.request();
            ReqInfo info = getReqInfoByRequest(request);
            try {
                info.setBody(getRequest(request.body()));
                info.setTimestamp(response.sentRequestAtMillis());
                info.setResp(getResponse(response));
                info.setStatusCode(response.code());
                info.setCostTime((int) (response.receivedResponseAtMillis() - info.getTimestamp()));
                info.setSuccess(response.isSuccessful());
                info.setUrlParam(request.url().query());
                info.setRemarks(String.format("%s Call.Enqueue Success !", className));
                collector.collect(info);
            } catch (Exception e) {
                // ig
            }
            callback.onResponse(call, response);
        }
    }


    private Object getRequest(RequestBody requestBody) {
        if (requestBody == null) {
            return null;
        }
        try (Buffer buffer = new Buffer()) {
            requestBody.writeTo(buffer);
            return URLDecoder.decode(buffer.readUtf8(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object getResponse(Response response) {
        if (response.body() != null) {
            BufferedSource source = response.body().source();
            try {
                source.request(Long.MAX_VALUE);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            Buffer buffer = source.getBuffer();
            Charset charset = StandardCharsets.UTF_8;
            MediaType mediaType = response.body().contentType();
            if (mediaType != null) {
                try {
                    charset = mediaType.charset(charset);
                } catch (UnsupportedCharsetException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            if (response.body().contentLength() != 0) {
                assert charset != null;
                try (Buffer cloneBuffer = buffer.clone()) {
                    return cloneBuffer.readString(charset);
                }
            }
        }
        return null;
    }

}
