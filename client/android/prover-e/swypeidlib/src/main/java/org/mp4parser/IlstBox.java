package org.mp4parser;

import android.util.SparseArray;

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

public class IlstBox extends AbstractContainerBox {
    public static final String TYPE = "ilst";

    private ByteBuffer header = ByteBuffer.allocate(8);

    public IlstBox() {
        super(TYPE);
    }

    public static <C> List<C> asList(SparseArray<C> sparseArray) {
        if (sparseArray == null) return null;
        List<C> arrayList = new ArrayList<C>(sparseArray.size());
        for (int i = 0; i < sparseArray.size(); i++)
            arrayList.add(sparseArray.valueAt(i));
        return arrayList;
    }

    @Override
    protected long getContainerSize() {
        List<Box> boxes = getBoxes();
        return super.getContainerSize() + boxes.size() * 8;
    }

    @Override
    public void parse(DataSource dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        int end = (int) (dataSource.position() + contentSize);

        SparseArray<Box> boxes = new SparseArray<>();
        while (dataSource.position() < end) {
            long startPos = dataSource.position();
            header.rewind();
            if (8 != dataSource.read(header)) {
                break;
            }
            header.rewind();
            int size = (int) IsoTypeReader.readUInt32(header);
            int num = (int) IsoTypeReader.readUInt32(header);

            Box box = boxParser.parseBox(dataSource, this);
            if (dataSource.position() != startPos + size) {
                dataSource.position(startPos + size);
            }
            boxes.append(num, box);
        }

        setBoxes(asList(boxes));
        dataSource.position(end);
    }

    @Override
    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        writableByteChannel.write(getHeader());

        List<Box> boxes = getBoxes();
        for (int i = 0; i < boxes.size(); i++) {
            Box box = boxes.get(i);
            int size = (int) (box.getSize() + 8);
            header.rewind().limit(8);
            IsoTypeWriter.writeUInt32(header, size);
            IsoTypeWriter.writeUInt32(header, i + 1);
            header.rewind();
            writableByteChannel.write(header);
            box.getBox(writableByteChannel);
        }
    }
}