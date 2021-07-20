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

/**
 * Created by EalenXie on 2021/7/15 11:16
 */
public class OkHttpClientProxy {


    private final ReqInfoCollector collector;

    private final OkHttpClient okHttpClient;

    private final String className = this.getClass().getName();

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


    public Response callExecute(Call call) throws IOException {
        Request request = call.request();
        Response response;
        long timestamp = System.currentTimeMillis();
        boolean success = false;
        Object req = null;
        Object resp = null;
        String desc = null;
        long responseTimestamp = 0;
        int rawStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        try {
            req = getRequest(request.body());
            response = call.execute();
            rawStatusCode = response.code();
            resp = getResponse(response);
            if (response.isSuccessful()) {
                success = true;
            }
            timestamp = response.sentRequestAtMillis();
            responseTimestamp = response.receivedResponseAtMillis();
            desc = String.format("%s Call.Execute Success !", className);
        } catch (Exception e) {
            desc = String.format("%s Call.Execute Fail. %s", className, e.getMessage());
            throw e;
        } finally {
            ReqInfo info = getReqInfoByRequest(request);
            info.setReq(req);
            info.setTimestamp(timestamp);
            info.setResp(resp);
            info.setStatusCode(rawStatusCode);
            if (responseTimestamp != 0) {
                info.setCostTime(responseTimestamp - timestamp);
            } else {
                info.setCostTime(System.currentTimeMillis() - timestamp);
            }
            info.setSuccess(success);
            info.setUrlParam(request.url().query());
            info.setDesc(desc);
            collector.collect(info);
        }
        return response;
    }

    public void callEnqueue(Call call, Callback responseCallback) {
        call.enqueue(new CallbackProxy(collector, responseCallback));
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

        public CallbackProxy(ReqInfoCollector collector, Callback callback) {
            this.collector = collector;
            this.callback = callback;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            Request request = call.request();
            ReqInfo info = getReqInfoByRequest(request);
            try {
                info.setReq(getRequest(request.body()));
                info.setTimestamp(System.currentTimeMillis());
                info.setResp(null);
                info.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                info.setCostTime(System.currentTimeMillis());
                info.setSuccess(false);
                info.setUrlParam(request.url().query());
                info.setDesc(String.format("%s Call.Enqueue Fail. %s", className, e.getMessage()));
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
                info.setReq(getRequest(request.body()));
                info.setTimestamp(response.sentRequestAtMillis());
                info.setResp(getResponse(response));
                info.setStatusCode(response.code());
                info.setCostTime(response.receivedResponseAtMillis() - info.getTimestamp());
                info.setSuccess(response.isSuccessful());
                info.setUrlParam(request.url().query());
                info.setDesc(String.format("%s Call.Enqueue Success !", className));
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
