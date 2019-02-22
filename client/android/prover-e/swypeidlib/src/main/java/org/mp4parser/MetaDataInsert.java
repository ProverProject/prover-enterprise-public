package org.mp4parser;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ChunkOffsetBox;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.FreeBox;
import com.coremedia.iso.boxes.HandlerBox;
import com.coremedia.iso.boxes.MovieBox;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.util.Path;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;

public class MetaDataInsert {

    public FileChannel splitFileAndInsert(File f, long pos, long length) throws IOException {
        FileChannel read = new RandomAccessFile(f, "r").getChannel();
        File tmp = File.createTempFile("ChangeMetaData", "splitFileAndInsert");
        FileChannel tmpWrite = new RandomAccessFile(tmp, "rw").getChannel();
        read.position(pos);
        tmpWrite.transferFrom(read, 0, read.size() - pos);
        read.close();
        FileChannel write = new RandomAccessFile(f, "rw").getChannel();
        write.position(pos + length);
        tmpWrite.position(0);
        long transferred = 0;
        while ((transferred += tmpWrite.transferTo(0, tmpWrite.size() - transferred, write)) != tmpWrite.size()) {
            //System.out.println(transferred);
        }
        //System.out.println(transferred);
        tmpWrite.close();
        tmp.delete();
        return write;
    }

    private boolean needsOffsetCorrection(IsoFile isoFile) {
        if (Path.getPath(isoFile, "/moov[0]/mvex[0]") != null) {
            // Fragmented files don't need a correction
            return false;
        } else {
            // no correction needed if mdat is before moov as insert into moov want change the offsets of mdat
            return Path.getPath(isoFile, "/moov[0]").getOffset() < Path.getPath(isoFile, "/mdat[0]").getOffset();
        }
    }

    public void writeMetadata(File videoFile, Map<String, String> tags) throws IOException {

        if (!videoFile.exists()) {
            throw new FileNotFoundException("File " + videoFile.getPath() + " not exists");
        }

        if (!videoFile.canWrite()) {
            throw new IllegalStateException("No write permissions to file " + videoFile.getPath());
        }
        IsoFile isoFile = new IsoFile(new FileDataSourceImpl(videoFile.getPath()));

        MovieBox moov = isoFile.getBoxes(MovieBox.class).get(0);
        FreeBox freeBox = findFreeBox(moov);

        boolean correctOffset = needsOffsetCorrection(isoFile);
        long sizeBefore = moov.getSize();
        long offset = moov.getOffset();

        BetterMetaBox metaBox;
        if ((metaBox = Path.getPath(moov, "meta")) == null) {
            metaBox = new BetterMetaBox();
            HandlerBox hdlr = new HandlerBox();
            hdlr.setHandlerType("mdir");
            metaBox.addBox(hdlr);
            moov.addBox(metaBox);
        }
        metaBox.addTags(tags);

        long sizeAfter = moov.getSize();
        long diff = sizeAfter - sizeBefore;
        // This is the difference of before/after


        // can we compensate by resizing a Free Box we have found?
        if (freeBox != null && freeBox.getData().limit() > diff) {
            // either shrink or grow!
            freeBox.setData(ByteBuffer.allocate((int) (freeBox.getData().limit() - diff)));
            sizeAfter = moov.getSize();
            diff = sizeAfter - sizeBefore;
        }
        if (correctOffset && diff != 0) {
            correctChunkOffsets(moov, diff);
        }
        BetterByteArrayOutputStream baos = new BetterByteArrayOutputStream();
        moov.getBox(Channels.newChannel(baos));
        isoFile.close();
        FileChannel fc;
        if (diff != 0) {
            // this is not good: We have to insert bytes in the middle of the file
            // and this costs time as it requires re-writing most of the file's data
            fc = splitFileAndInsert(videoFile, offset, sizeAfter - sizeBefore);
        } else {
            // simple overwrite of something with the file
            fc = new RandomAccessFile(videoFile, "rw").getChannel();
        }
        fc.position(offset);
        fc.write(ByteBuffer.wrap(baos.getBuffer(), 0, baos.size()));
        fc.force(true);
        fc.close();
    }

    FreeBox findFreeBox(Container c) {
        for (Box box : c.getBoxes()) {
            //System.err.println(box.getType());
            if (box instanceof FreeBox) {
                return (FreeBox) box;
            }
            if (box instanceof Container) {
                FreeBox freeBox = findFreeBox((Container) box);
                if (freeBox != null) {
                    return freeBox;
                }
            }
        }
        return null;
    }

    private void correctChunkOffsets(MovieBox movieBox, long correction) {
        List<ChunkOffsetBox> chunkOffsetBoxes = Path.getPaths((Box) movieBox, "trak/mdia[0]/minf[0]/stbl[0]/stco[0]");
        if (chunkOffsetBoxes.size() == 0) {
            chunkOffsetBoxes = Path.getPaths((Box) movieBox, "trak/mdia[0]/minf[0]/stbl[0]/st64[0]");
        }
        for (ChunkOffsetBox chunkOffsetBox : chunkOffsetBoxes) {
            long[] cOffsets = chunkOffsetBox.getChunkOffsets();
            for (int i = 0; i < cOffsets.length; i++) {
                cOffsets[i] += correction;
            }
        }
    }

    private static class BetterByteArrayOutputStream extends ByteArrayOutputStream {
        byte[] getBuffer() {
            return buf;
        }
    }
}