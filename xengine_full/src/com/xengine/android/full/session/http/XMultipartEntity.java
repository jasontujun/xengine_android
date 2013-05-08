package com.xengine.android.full.session.http;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class XMultipartEntity extends MultipartEntity {

    private final XHttpTransferListener listener;

    public XMultipartEntity(XHttpTransferListener listener) {
        super();
        this.listener = listener;
    }

    public XMultipartEntity(HttpMultipartMode mode, XHttpTransferListener listener) {
        super(mode);
        this.listener = listener;
    }

    public XMultipartEntity(HttpMultipartMode mode, String boundary, Charset charset, XHttpTransferListener listener) {
        super(mode, boundary, charset);
        this.listener = listener;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        super.writeTo(new CountingOutputStream(out, listener));
    }

    public static class CountingOutputStream extends FilterOutputStream {
        private final XHttpTransferListener listener;
        private long transferred;

        public CountingOutputStream(final OutputStream out, final XHttpTransferListener listener) {
            super(out);
            this.listener = listener;
            this.transferred = 0;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            this.transferred += len;
            this.listener.transferred(this.transferred);
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            this.transferred++;
            this.listener.transferred(this.transferred);
        }
    }
}
