package com.xengine.android.session.http.apache;

import com.xengine.android.session.http.XBaseHttpResponse;
import com.xengine.android.session.http.XHttp;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-6
 * Time: 下午7:55
 * To change this template use File | Settings | File Templates.
 */
class XApacheHttpResponse extends XBaseHttpResponse {

    private HttpEntity mEntity;

    protected void setEntity(HttpEntity entity) {
        mEntity = entity;
    }

    @Override
    public void consumeContent() {
        super.consumeContent();
        if (mEntity != null)
            try {
                mEntity.consumeContent();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public Charset getContentType() {
        if (mEntity == null)
            return null;

        Charset charset = null;
        // 先调用ContentType去尝试解析字符编码
        ContentType contentType = ContentType.get(mEntity);
        if (contentType != null) {
            charset = contentType.getCharset();
        }
        // 如果解析失败，则使用默认编码
        if (charset == null) {
            charset = XHttp.DEF_CONTENT_CHARSET;
        }
        return charset;
    }
}
