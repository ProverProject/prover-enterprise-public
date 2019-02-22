package io.prover.common.enterprise.transport;

import org.json.JSONException;
import org.json.JSONObject;

import io.prover.common.enterprise.transport.response.SwypeCodeFastReply;
import io.prover.common.enterprise.transport.response.SwypeCodeReply;
import io.prover.common.transport.ISwypeCodeOrderResult;
import io.prover.common.transport.OrderType;
import io.prover.common.util.Base64;

public class OrderRequestData {
    public final String message;
    public final byte[] digest;
    public final int clientId;
    public final OrderType type;
    public final ISwypeCodeOrderResult swypeCodeOrderResult;
    private final String KEY_MESSAGE = "m";
    private final String KEY_DIGEST = "d";
    private final String KEY_CLIENT_ID = "id";
    private final String KEY_TYPE = "t";

    public OrderRequestData(OrderType type, String message, byte[] digest, int clientId, ISwypeCodeOrderResult swypeCodeOrderResult) {
        this.type = type;
        this.message = message;
        this.digest = digest;
        this.clientId = clientId;
        this.swypeCodeOrderResult = swypeCodeOrderResult;
    }

    public OrderRequestData(JSONObject src) throws JSONException {
        message = src.isNull(KEY_MESSAGE) ? null : src.getString(KEY_MESSAGE);
        digest = src.isNull(KEY_DIGEST) ? null : Base64.decode(src.getString(KEY_DIGEST));
        clientId = src.getInt(KEY_CLIENT_ID);
        type = OrderType.valueOf(src.getString(KEY_TYPE));
        swypeCodeOrderResult = null;
    }

    public static OrderRequestData requestQrCode(String message, int clientId) {
        return new OrderRequestData(OrderType.QrCode, message, null, clientId, null);
    }

    public static OrderRequestData requestSwypeCode(boolean isFast) {
        return new OrderRequestData(isFast ? OrderType.SwypeFast : OrderType.SwypeFull, null, null, 0, null);
    }

    public static OrderRequestData submitFileHash(byte[] digest, ISwypeCodeOrderResult swypeCodeOrderResult, int clientId) {
        return new OrderRequestData(OrderType.FileHash, null, digest, clientId, swypeCodeOrderResult);
    }

    public MediaHashRequestData toMediaRequestData() {
        if (swypeCodeOrderResult instanceof SwypeCodeFastReply) {
            return new MediaHashRequestData(digest, MediaHashRequestData.MediHashType.SHA256, clientId, (SwypeCodeFastReply) swypeCodeOrderResult);
        } else if (swypeCodeOrderResult instanceof SwypeCodeReply) {
            return new MediaHashRequestData(digest, MediaHashRequestData.MediHashType.SHA256, clientId, (SwypeCodeReply) swypeCodeOrderResult);
        }
        throw new RuntimeException("Not implemented for class: " + swypeCodeOrderResult.getClass().getName());
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (message != null)
            json.put(KEY_MESSAGE, message);
        if (digest != null)
            json.put(KEY_DIGEST, Base64.encodeBase64(digest));
        json.put(KEY_TYPE, type.name());
        json.put(KEY_CLIENT_ID, clientId);
        return json;
    }
}
