package io.prover.swypeid.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class IntKeeper {
    public final int segmentSize = 1000;
    private final List<int[]> segments = new ArrayList<>();
    private int amount = 0;

    public void add(int value) {
        int seg = amount / segmentSize;
        int pos = amount % segmentSize;
        if (segments.size() <= seg)
            segments.add(new int[segmentSize]);
        int[] segment = segments.get(seg);
        segment[pos] = value;
        ++amount;
    }

    public int getSegmentSize() {
        return segmentSize;
    }

    public int getAmount() {
        return amount;
    }

    public void clear() {
        segments.clear();
        amount = 0;
    }

    public int[] getIntValues() {
        if (amount == 0)
            return new int[0];

        int[] result = new int[amount];
        for (int i = 0; i < segments.size(); i++) {
            int[] segment = segments.get(i);
            int copyAmount = i < segments.size() - 1 ? segmentSize : amount % segmentSize;
            System.arraycopy(segment, 0, result, i * segmentSize, copyAmount);
        }
        return result;
    }

    public byte[] getValuesAsBigEndianBytes() {
        byte[] bytes = new byte[amount * 4];
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.BIG_ENDIAN);
        IntBuffer buf = bb.asIntBuffer();
        for (int i = 0; i < segments.size(); i++) {
            int[] segment = segments.get(i);
            int copyAmount = i < segments.size() - 1 ? segmentSize : amount % segmentSize;
            buf.put(segment, 0, copyAmount);
        }/*
        StringBuilder bld = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            bld.append(String.format("%02x", bytes[i] & 0xff));
            if (i%4==3)
                bld.append(" ");

        }
        Log.d("GenDebug", "result: " + bld.toString());*/

        return bytes;
    }
}
