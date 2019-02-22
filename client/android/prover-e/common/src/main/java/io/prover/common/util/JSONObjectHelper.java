package io.prover.common.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

import io.prover.common.util.encoders.Hex;

public class JSONObjectHelper {

    public final JSONObject json;

    public JSONObjectHelper(JSONObject json) {
        this.json = json;
    }

    /**
     * parses string like 0x203040 as unsigned integer to BigInteger
     *
     * @param name     -- field name in json
     * @param remove0x -- true if value starts with 0x
     * @return
     */
    @Nullable
    public BigInteger getUnsignedBigInteger(String name, boolean remove0x) throws JSONException {
        if (json.isNull(name))
            return null;

        String src = json.getString(name);
        if (src == null)
            return null;

        if (remove0x)
            src = src.substring(2);

        if (src.length() % 2 == 1) {
            src = "0" + src;
        }
        byte[] bytes = Hex.decode(src);
        return BigIntegers.fromUnsignedByteArray(bytes);
    }

    @Nullable
    public byte[] parseHexAsByteArray(String name, int expectedLength) throws JSONException {
        if (json.isNull(name))
            return null;

        String src = json.getString(name);
        if (src == null || src.length() < 2)
            return null;

        if (src.indexOf('x') != -1) {
            src = src.substring(2);
        }
        if (src.length() % 2 == 1) {
            src = "0" + src;
        }
        byte[] hex = Hex.decode(src);
        if (hex.length == expectedLength)
            return hex;
        if (hex.length < expectedLength) {
            byte[] result = new byte[expectedLength];
            System.arraycopy(hex, 0, result, expectedLength - hex.length, hex.length);
            return result;
        }
        throw new IllegalArgumentException(String.format("Expected length: %d, actual: %d", expectedLength, hex.length));
    }

    /**
     * parses int array; return int[0] if it does not exist
     *
     * @param name
     * @return
     */
    @NonNull
    public int[] getIntArray(String name) throws JSONException {
        if (json.isNull(name))
            return new int[0];
        JSONArray array = json.getJSONArray(name);
        int[] result = new int[array.length()];
        for (int i = 0; i < result.length; i++) {
            result[i] = array.getInt(i);
        }
        return result;
    }

    /**
     * same as JSONObject.getString(), but return null if mapping does not exist
     *
     * @param name
     * @return
     * @throws JSONException
     */
    @Nullable
    public String getStringNullable(String name) throws JSONException {
        if (json.isNull(name))
            return null;
        return json.getString(name);
    }

/*    public Instant getInstant(String name) throws JSONException {
        String instantStr = source.getString(name);
        return Instant.parse(instantStr);
    }


    public JSONObjectHelper put(String name, Instant time) throws JSONException {
        source.put(name, DateTimeFormatter.ISO_INSTANT.format(time));
        return this;
    }

    */

    public JSONObjectHelper put(String name, BigInteger value) throws JSONException {
        if (value != null) {
            byte[] bytes = BigIntegers.asUnsignedByteArray(value);
            String s = Hex.toHexString(bytes);
            json.put(name, "0x" + s);
        }
        return this;
    }

    public JSONObjectHelper put(String name, int[] numbers) throws JSONException {
        if (numbers != null) {
            JSONArray arr = new JSONArray();
            for (int i = 0; i < numbers.length; i++) {
                arr.put(numbers[i]);
            }
            json.put(name, arr);
        }
        return this;
    }

    public JSONObjectHelper putAsHexString(String name, byte[] bytes, boolean add0x) throws JSONException {
        String s = Hex.toHexString(bytes);
        if (add0x)
            json.put(name, "0x" + s);
        else
            json.put(name, s);
        return this;
    }
}
