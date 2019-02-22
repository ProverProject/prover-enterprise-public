package org.mp4parser;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.AbstractContainerBox;
import com.googlecode.mp4parser.DataSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

public class KeysBox extends AbstractContainerBox {
    public static final String TYPE = "keys";

    public KeysBox() {
        super(TYPE);
    }

    @Override
    public void parse(DataSource dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        int start = (int) dataSource.position();
        ByteBuffer buffer = ByteBuffer.allocate(l2i(contentSize));
        dataSource.read(buffer);
        buffer.position(0);

        buffer.getInt(); // 0;
        int amount = (int) IsoTypeReader.readUInt32(buffer);

        dataSource.position(start + 8);
        List<Box> boxes = new ArrayList<>();
        for (int i = 0; i < amount; ++i) {
            Box box = boxParser.parseBox(dataSource, this);
            boxes.add(box);
        }
        setBoxes(boxes);
        dataSource.position(start + contentSize);
    }

    @Override
    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        writableByteChannel.write(getHeader());
        ByteBuffer buffer = ByteBuffer.allocate(8);
        IsoTypeWriter.writeUInt32(buffer, 0);
        IsoTypeWriter.writeUInt32(buffer, getBoxes().size());
        buffer.rewind();
        writableByteChannel.write(buffer);
        writeContainer(writableByteChannel);
    }

    @Override
    protected long getContainerSize() {
        return super.getContainerSize() + 8;
    }
}