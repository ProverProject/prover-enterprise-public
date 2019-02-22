package io.prover.common.transport;

import org.json.JSONException;
import org.json.JSONObject;

public interface IQrCodeOrderResult {
    String getMessage();

    byte[] getQrCodeBytes();

    JSONObject toJson() throws JSONException;
}
