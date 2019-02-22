package io.prover.common.enterprise.transport.response;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import io.prover.common.transport.IQrCodeOrderResult;
import io.prover.common.util.JSONObjectHelper;

public class QrCodeReply implements IPendingReply, IQrCodeOrderResult {

    private static final String KEY_TXHASH = "txhash";
    private static final String KEY_REFERENCE_BLOCK_HASH = "referenceBlockHash";
    private static final String KEY_MESSAGE_SIGNATURE = "messageSignature";
    private static final String KEY_REFERENCE_BLOCK_HEIGHT = "referenceBlockHeight";
    private static final String KEY_CONFIRMATIONS = "confirmations";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_MESSAGE = "message";

    public final byte[] txHash;
    public final Integer referenceBlockHeight;
    public final byte[] referenceBlockHash;
    public final byte[] messageSignature;
    public final int confirmations;
    public final Integer height;
    public final boolean isPending;
    public final String message;

/*
{
  "result":
  {
    "txhash": "f215f2be2a3fd7daa60c99290f401a102c788002493210dae9795ba7d98b16f2",
    "referenceBlockHeight": 1881912,
    "referenceBlockHash": "953f8f0fe4f850548fc496f9d6f7e25c94049f7891d01f7e1c096582ba69a420",
    "messageSignature": "253ca49fb4c16b45c0bc"
  }
}
QR-code is generated from the reference block height, the reference block hash and the message signature.

Response (polling, transaction is not confirmed)
{
  "result":
  {
    "confirmations": 0,
    "height": null
  }
}
Response (polling, transaction is confirmed)
{
  "result":
  {
    "confirmations": 2,
    "height": 1881915
  }
}
 */

    public QrCodeReply(JSONObject source, QrCodeReply firstReply, String message) throws JSONException {
        if (message != null)
            this.message = message;
        else
            this.message = source.optString(KEY_MESSAGE);

        if (firstReply == null) {
            JSONObjectHelper helper = new JSONObjectHelper(source);
            txHash = helper.parseHexAsByteArray(KEY_TXHASH, 32);
            referenceBlockHash = helper.parseHexAsByteArray(KEY_REFERENCE_BLOCK_HASH, 32);
            messageSignature = helper.parseHexAsByteArray(KEY_MESSAGE_SIGNATURE, 10);
            referenceBlockHeight = source.isNull(KEY_REFERENCE_BLOCK_HEIGHT) ? null :
                    source.getInt(KEY_REFERENCE_BLOCK_HEIGHT);
        } else {
            this.txHash = firstReply.txHash;
            this.messageSignature = firstReply.messageSignature;
            this.referenceBlockHeight = firstReply.referenceBlockHeight;
            this.referenceBlockHash = firstReply.referenceBlockHash;
        }

        confirmations = source.isNull(KEY_CONFIRMATIONS) ? 0 : source.getInt(KEY_CONFIRMATIONS);

        if (source.isNull(KEY_HEIGHT)) {
            this.height = null;
            isPending = true;
        } else {
            this.height = source.getInt(KEY_HEIGHT);
            isPending = false;
        }
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONObjectHelper helper = new JSONObjectHelper(jsonObject);
        helper.putAsHexString(KEY_TXHASH, txHash, false);
        helper.putAsHexString(KEY_REFERENCE_BLOCK_HASH, referenceBlockHash, false);
        helper.putAsHexString(KEY_MESSAGE_SIGNATURE, messageSignature, false);
        if (referenceBlockHeight != null)
            jsonObject.put(KEY_REFERENCE_BLOCK_HEIGHT, referenceBlockHeight);
        if (confirmations > 0)
            jsonObject.put(KEY_CONFIRMATIONS, confirmations);
        if (height != null)
            jsonObject.put(KEY_HEIGHT, height);

        jsonObject.put(KEY_MESSAGE, message);

        return jsonObject;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * QR code equals:
     * referenceBlockHeight as littleEndian (4 bytes) + referenceBlockHash + messageSignature
     *
     * @return qr code as byte array (46 bytes)
     */
    @Override
    public byte[] getQrCodeBytes() {
        if (height == null) {
            throw new IllegalStateException("responce is still pending");
        }
        ByteBuffer buf = ByteBuffer.allocate(46).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(height).put(referenceBlockHash).put(messageSignature);

        return buf.array();
    }

    @Override
    public boolean isStillPending() {
        return isPending;
    }
}
