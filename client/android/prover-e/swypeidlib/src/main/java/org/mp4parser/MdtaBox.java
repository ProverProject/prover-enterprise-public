package org.mp4parser;

import com.coremedia.iso.IsoTypeReader;
import com.googlecode.mp4parser.AbstractBox;

import java.nio.ByteBuffer;

public class MdtaBox extends AbstractBox {
    public static final String TYPE = "mdta";

    private String key;

    public MdtaBox() {
        super(TYPE);
    }

    public MdtaBox(String key) {
        super(TYPE);
        this.key = key;
    }

    @Override
    protected long getContentSize() {
        return key.getBytes().length;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        byteBuffer.put(key.getBytes());
    }

    @Override
    protected void _parseDetails(ByteBuffer data) {
        key = IsoTypeReader.readString(data, data.remaining());
    }

    public String getKey() {
        return key;
    }
}