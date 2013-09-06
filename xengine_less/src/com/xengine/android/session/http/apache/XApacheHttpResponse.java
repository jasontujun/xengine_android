package com.xengine.android.session.http.apache;

import com.xengine.android.session.http.XBaseHttpResponse;
import org.apache.http.HttpEntity;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-6
 * Time: 下午7:55
 * To change this template use File | Settings | File Templates.
 */
public class XApacheHttpResponse extends XBaseHttpResponse {

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
}
