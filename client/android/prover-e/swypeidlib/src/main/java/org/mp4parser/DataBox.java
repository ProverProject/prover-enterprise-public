package org.mp4parser;

import com.coremedia.iso.IsoTypeReader;
import com.googlecode.mp4parser.AbstractBox;

import java.nio.ByteBuffer;

public class DataBox extends AbstractBox {
    public static final String TYPE = "data";
    boolean isParsed = false;
    private int type;
    private int locale;
    private byte[] data;

    public DataBox() {
        super(TYPE);
    }

    public DataBox(int locale, String data) {
        super(TYPE);
        this.type = 1;
        this.locale = locale;
        this.data = data.getBytes();
        isParsed = true;
    }

    public void setData(int locale, String data) {
        this.type = 1;
        this.locale = locale;
        this.data = data.getBytes();
        isParsed = true;
    }

    @Override
    public boolean isParsed() {
        return isParsed || super.isParsed();
    }

    @Override
    protected long getContentSize() {
        return 8 + data.length;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        byteBuffer.putInt(type);
        byteBuffer.putInt(locale);
        byteBuffer.put(data);
    }

    @Override
    protected void _parseDetails(ByteBuffer content) {
        type = (int) IsoTypeReader.readUInt32(content);
        locale = (int) IsoTypeReader.readUInt32(content);
        data = IsoTypeReader.readString(content, content.remaining()).getBytes();
        isParsed = true;
    }
}