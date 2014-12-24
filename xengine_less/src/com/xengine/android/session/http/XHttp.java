package com.xengine.android.session.http;

import org.apache.http.cookie.Cookie;

import java.nio.charset.Charset;

public interface XHttp {

    public static final Charset DEF_CONTENT_CHARSET = Charset.forName("ISO-8859-1");
    public static final Charset DEF_PROTOCOL_CHARSET = Charset.forName("US-ASCII");

    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String GZIP = "gzip";

    /**
     * 创建一个Http请求对象。默认为GET请求
     * @param url
     * @return
     */
    XHttpRequest newRequest(String url);

    /**
     * 执行http请求
     * @param req
     * @return
     */
    XHttpResponse execute(XHttpRequest req);

    /**
     * 终止当前的http请求
     */
    void abort();

    /**
     * 当前HttpClient是否停止
     * @return
     */
    boolean isDisposed();

    /**
     * 停止HttpClient
     */
    void dispose();

    /**
     * 添加Cookie
     * @param cookie
     */
    void addCookie(Cookie cookie);

    /**
     * 设置Cookie。
     * 如果cookie的name出现重复，则覆盖之前的cookie。
     * @param cookie
     */
    void setCookie(Cookie cookie);

    /**
     * 清空Cookie
     */
    void clearCookie();

    /**
     * 获取连接超时的设定值
     * @return
     */
    int getConnectionTimeOut();

    /**
     * 设置连接超时的值，默认为10秒
     * @param connectionTimeOut 单位：毫秒
     */
    void setConnectionTimeOut(int connectionTimeOut);

    /**
     * 获取响应超时的设定值
     * @return
     */
    int getResponseTimeOut();

    /**
     * 设置响应超时的值，默认为10秒
     * @param responseTimeOut 单位：毫秒
     */
    void setResponseTimeOut(int responseTimeOut);

    /**
     * 注册对http请求过程的监听
     * @param listener
     */
    void registerProgressListener(XHttpProgressListener listener);

    /**
     * 取消对htp请求过程的监听
     * @param listener
     */
    void unregisterProgressListener(XHttpProgressListener listener);
}
