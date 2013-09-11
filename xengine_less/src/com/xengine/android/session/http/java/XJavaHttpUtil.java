package com.xengine.android.session.http.java;

import android.text.TextUtils;
import com.xengine.android.utils.XLog;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MIME;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.ByteArrayBuffer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Random;

/**
 * 用于生成XJavaHttpRequest的工具类。
 * @see XJavaHttpRequest
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-6
 * Time: 下午7:06
 * To change this template use File | Settings | File Templates.
 */
public class XJavaHttpUtil {

    private static final String TAG = XJavaHttpUtil.class.getSimpleName();

    /**
     * The pool of ASCII chars to be used for generating a multipart boundary.
     */
    private final static char[] MULTIPART_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    .toCharArray();
    public static final ByteArrayBuffer FIELD_SEP = encode(MIME.DEFAULT_CHARSET, ": ");
    public static final ByteArrayBuffer CR_LF = encode(MIME.DEFAULT_CHARSET, "\r\n");
    public static final ByteArrayBuffer TWO_DASHES = encode(MIME.DEFAULT_CHARSET, "--");


    private static ByteArrayBuffer encode(
            final Charset charset, final String string) {
        ByteBuffer encoded = charset.encode(CharBuffer.wrap(string));
        ByteArrayBuffer bab = new ByteArrayBuffer(encoded.remaining());
        bab.append(encoded.array(), encoded.position(), encoded.remaining());
        return bab;
    }


    public static void writeBytes(
            final ByteArrayBuffer b, final OutputStream out) throws IOException {
        out.write(b.buffer(), 0, b.length());
    }

    public static void writeBytes(
            final String s, final Charset charset, final OutputStream out) throws IOException {
        ByteArrayBuffer b = encode(charset, s);
        writeBytes(b, out);
    }

    public static void writeBytes(
            final String s, final OutputStream out) throws IOException {
        ByteArrayBuffer b = encode(MIME.DEFAULT_CHARSET, s);
        writeBytes(b, out);
    }

    /**
     * 生成Http消息头中的ContentType的值。
     * Content-Type为multipart/form-data
     * @param boundary
     * @param charset
     * @return
     */
    public static String generateMultiContentType(
            final String boundary,
            final String charset) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("multipart/form-data; boundary=");
        buffer.append(boundary);
        if (charset != null) {
            buffer.append("; charset=");
            buffer.append(charset);
        }
        return buffer.toString();
    }

    /**
     * 生成Http消息头中的ContentType的值
     * Content-Type为application/x-www-form-urlencoded
     * @param charset
     * @return
     */
    public static String generateStringContentType(
            final String charset) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("application/x-www-form-urlencoded");
        if (charset != null) {
            buffer.append("; charset=");
            buffer.append(charset);
        }
        return buffer.toString();
    }

    /**
     * 生成一个随机的Boundary字符串
     * @return
     */
    public static String generateBoundary() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(11) + 30; // a random size from 30 to 40
        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }

    /**
     * 获取文件的上传类型，图片格式为image/png,image/jpg等。
     * 非图片为application/octet-stream
     * @param f
     * @return
     */
    public static String getContentType(File f) {
        int dotIndex = f.getAbsolutePath().lastIndexOf(".");
        if (dotIndex < 0) {
            return "application/octet-stream";
        }

        String suffix = f.getAbsolutePath().substring(dotIndex).toLowerCase();
        if ("jpg".equals(suffix)
                || "jpeg".equals(suffix))
            return "image/jpeg";
        else if ("png".equals(suffix)
                || "gif".equals(suffix))
            return "image/" + suffix;
        else if ("mp3".equals(suffix)
                || "mpeg".equals(suffix))
            return "audio/mpeg";
        else if ("mp4".equals(suffix)
                || "ogg".equals(suffix))
            return "audio/" + suffix;

        return "application/octet-stream";
    }


    /**
     * 根据Set-Cookie字段的字符串生成Cookie对象
     * @param cookieStr
     * @return 如果字符串合法，返回Cookie对象；否则返回null
     */
    public static Cookie createCookie(String cookieStr) {
        if (TextUtils.isEmpty(cookieStr))
            return null;

        String name;
        String value;
        int start = 0;
        BasicClientCookie cookie = null;
        // 去除空格
        cookieStr = cookieStr.replaceAll(" ", "");
        // 获取name和value生成cookie，是第一个分号；
        int equalSignIndex = cookieStr.indexOf("=", start);
        int semicolonIndex = cookieStr.indexOf(";", start);
        if (equalSignIndex != -1) {
            name = cookieStr.substring(start, equalSignIndex);
            if (semicolonIndex == -1)
                value = cookieStr.substring(equalSignIndex + 1);
            else
                value = cookieStr.substring(equalSignIndex + 1, semicolonIndex);
            cookie = new BasicClientCookie(name, value);
            start = semicolonIndex + 1;
        } else {
            return cookie;
        }
        // 获取cookie的相关属性
        while (0 < start && start < cookieStr.length()) {
            equalSignIndex = cookieStr.indexOf("=", start);
            semicolonIndex = cookieStr.indexOf(";", start);
            if (equalSignIndex != -1) {
                name = cookieStr.substring(start, equalSignIndex);
                if (semicolonIndex == -1)
                    value = cookieStr.substring(equalSignIndex + 1);
                else
                    value = cookieStr.substring(equalSignIndex + 1, semicolonIndex);
                if (name.equalsIgnoreCase("path"))
                    cookie.setPath(value);
                else if (name.equalsIgnoreCase("domain"))
                    cookie.setDomain(value);
                else
                    cookie.setAttribute(name, value);
            }
            start = semicolonIndex + 1;
        }
        return cookie;
    }


    /**
     * 将Cookie中的domain和path值和请求的url进行筛选匹配。
     * 匹配规则：
     * 1.请求的主机名是否与某个存储的Cookie的Domain属性尾部匹配；如acme.com的domain将与主机名anvil.acme.com匹配；
     * 2.请求的端口号是否在该Cookie的Port属性列表中；
     * 3.请求的资源路径是否在该Cookie的Path属性指定的目录及子目录中，即是否与Path属性头部匹配；
     * 4.该Cookie的有效期是否已过。
     * 结果：
     * 如果匹配，则返回true(验证通过，会把该Cookie加入这个请求中)；
     * 如果不匹配，返回false。
     * 注意：url的格式规范为： scheme://host.domain:port/path/filename
     * @param cookie
     * @param url
     * @return
     */
    public static boolean verifyCookie(Cookie cookie, String url)  {
        if (cookie == null ||TextUtils.isEmpty(url))
            return false;

        XLog.d(TAG, "$$$:" + cookie);
        XLog.d(TAG, "url:" + url);
        // 如果Cookie没有设置domain，则返回false
        if (TextUtils.isEmpty(cookie.getDomain()))
            return false;

        int firstDotIndex = url.indexOf(".");// 第一个点
        if (firstDotIndex < 0)
            return false;
        int firstColonIndex = url.indexOf(":", firstDotIndex);// 第一个冒号
        int firstSlashIndex = url.indexOf("/", firstDotIndex);// 第一个斜杠
        XLog.d(TAG, "firstDotIndex:" + firstDotIndex
                + ",firstColonIndex:" + firstColonIndex
                + ",firstSlashIndex:" + firstSlashIndex);
        // ================== 先匹配domain(尾部匹配) ==================
        String cookieDomain = cookie.getDomain().toLowerCase();
        // 提取url中的domain
        String urlDomain;
        if (firstColonIndex > 0)
            urlDomain = url.substring(firstDotIndex, firstColonIndex);
        else if (firstSlashIndex > 0)
            urlDomain = url.substring(firstDotIndex, firstSlashIndex);
        else
            urlDomain = url.substring(firstDotIndex);
        urlDomain = urlDomain.toLowerCase();
        if (!urlDomain.endsWith(cookieDomain))
            return false;

        // ================== 再匹配path(头部匹配) ==================
        // 如果path为空，则返回true
        if (TextUtils.isEmpty(cookie.getPath()))
            return true;
        String cookiePath = cookie.getPath().toLowerCase();
        // 提取url中的path
        String urlPath = null;
        if (firstSlashIndex < 0)
            urlPath = "/";
        else
            urlPath = url.substring(firstSlashIndex);
        urlPath = urlPath.toLowerCase();
        if (urlPath.startsWith(cookiePath))
            return true;
        else
            return false;
    }


    /**
     * 输入Content-Type的值，获取响应头中的字符编码。例：
     * Content-Type:text/html;charset=ISO-8859-1
     * @param contentTypeValue
     * @return
     */
    public static Charset getResponseCharset(String contentTypeValue) {
        if (TextUtils.isEmpty(contentTypeValue))
            return null;

        // 去除空格
        contentTypeValue = contentTypeValue.replaceAll(" ", "");
        // 提取charset字段的值
        final String tag = "charset";
        int tagIndex = contentTypeValue.indexOf(tag);
        if (tagIndex == -1)
            return null;

        String charsetName;
        int semicolonIndex = contentTypeValue.indexOf(";", tagIndex);
        if (semicolonIndex == -1)
            charsetName = contentTypeValue.substring(tagIndex + tag.length() + 1);
        else
            charsetName = contentTypeValue.substring(tagIndex + tag.length() + 1, semicolonIndex);
        return Charset.forName(charsetName);
    }

}
