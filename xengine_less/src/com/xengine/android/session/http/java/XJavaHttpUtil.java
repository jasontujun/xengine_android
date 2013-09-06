package com.xengine.android.session.http.java;

import org.apache.http.entity.mime.MIME;
import org.apache.http.util.ByteArrayBuffer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-6
 * Time: 下午7:06
 * To change this template use File | Settings | File Templates.
 */
public class XJavaHttpUtil {
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
     * 生成Http消息头中的ContentType的值
     * @param boundary
     * @param charset
     * @return
     */
    public static String generateContentType(
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
        if (!"png".equals(suffix)
                && !"jpg".equals(suffix)
                && !"gif".equals(suffix)
                && !"bmp".equals(suffix))
            return "application/octet-stream";
        else
            return "image/" + suffix;

    }
}
