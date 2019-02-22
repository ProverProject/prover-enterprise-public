package io.prover.clapperboard;

import org.json.JSONException;
import org.json.JSONObject;

import io.prover.common.transport.IQrCodeOrderResult;
import io.prover.common.util.Base64;

public class QrCodeOrderData implements IQrCodeOrderResult {

    private static final String KEY_MESSAGE_DATA = "md";
    private static final String KEY_CODE_BYTES = "b";

    private final String message;
    private final byte[] qrCodeBytes;

    public QrCodeOrderData(IQrCodeOrderResult source) {
        this.message = source.getMessage();
        this.qrCodeBytes = source.getQrCodeBytes();
    }

    public QrCodeOrderData(JSONObject src) throws JSONException {
        qrCodeBytes = src.isNull(KEY_CODE_BYTES) ? null : Base64.decode(src.getString(KEY_CODE_BYTES).toCharArray());
        message = src.isNull(KEY_MESSAGE_DATA) ? null : src.getString(KEY_MESSAGE_DATA);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public byte[] getQrCodeBytes() {
        return qrCodeBytes;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject jso = new JSONObject();

        if (qrCodeBytes != null)
            jso.put(KEY_CODE_BYTES, Base64.encodeBase64(qrCodeBytes));

        if (message != null)
            jso.put(KEY_MESSAGE_DATA, message);

        return jso;
    }
}
