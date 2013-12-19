package com.xengine.android.session.http;

import android.text.TextUtils;
import org.apache.http.util.CharArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: jasontujun
 * Date: 13-9-10
 * Time: 下午6:35
 * To change this template use File | Settings | File Templates.
 */
public class XHttpUtil {

    /**
     * Get the entity content as a String, using the provided default character set
     * if none is found in the entity.
     * If defaultCharset is null, the default "ISO-8859-1" is used.
     * @param response must not be null
     * @param defaultCharsetStr character set to be applied if none found in the entity
     * @return the entity content as a String. May be null if
     */
    public static String toString(final XHttpResponse response,
                                  final String defaultCharsetStr) throws IOException {
        if (response == null)
            throw new IllegalArgumentException("HTTP entity may not be null");

        InputStream instream = response.getContent();
        if (instream == null)
            return null;

        Charset defaultCharset = null;
        if (!TextUtils.isEmpty(defaultCharsetStr))
            defaultCharset = Charset.forName(defaultCharsetStr);
        try {
            if (response.getContentLength() > Integer.MAX_VALUE)
                throw new IllegalArgumentException
                        ("HTTP entity too large to be buffered in memory");

            int i = (int)response.getContentLength();
            if (i < 0) {
                i = 4096;
            }
            Charset charset = response.getContentType();
            if (charset == null) {
                charset = defaultCharset;
            }
            if (charset == null) {
                charset = XHttp.DEF_CONTENT_CHARSET;
            }
            Reader reader = new InputStreamReader(instream, charset);
            CharArrayBuffer buffer = new CharArrayBuffer(i);
            char[] tmp = new char[1024];
            int l;
            while((l = reader.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
            }
            return buffer.toString();
        } finally {
            instream.close();
        }
    }

    /**
     * Read the contents of an entity and return it as a String.
     * The content is converted using the character set from the entity (if any),
     * failing that, "ISO-8859-1" is used.
     * @param response
     * @return String containing the content.
     * @throws org.apache.http.ParseException if header elements cannot be parsed
     * @throws IllegalArgumentException if entity is null or if content length > Integer.MAX_VALUE
     * @throws java.io.IOException if an errorUndone occurs reading the input stream
     */
    public static String toString(final XHttpResponse response) throws IOException {
        return toString(response, null);
    }

}
