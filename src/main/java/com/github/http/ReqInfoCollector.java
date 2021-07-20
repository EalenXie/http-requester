package com.github.http;

/**
 * Created by EalenXie on 2021/7/14 15:36
 */
public interface ReqInfoCollector {


    /**
     * 请求详情对象
     *
     * @param reqInfo 请求详情对象
     */
    void collect(ReqInfo reqInfo);
}
