package io.prover.common.enterprise.transport.request;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import io.prover.common.transport.base.FormRequest;
import io.prover.common.transport.base.INetworkRequestListener;
import io.prover.common.transport.exception.ServerException;
import okhttp3.OkHttpClient;

public abstract class ProverEnterpriseRequest<Result> extends FormRequest<Result> {

    ProverEnterpriseRequest(OkHttpClient client, Uri server, String method, INetworkRequestListener<Result> listener) {
        super(client, server.toString(), "/ent/v1/" + method, listener);
    }

    @Override
    public ProverEnterpriseRequest<Result> execute() {
        super.execute();
        return this;
    }

    @Override
    protected boolean isResponseOk(String response, int code) {
        if (code != 200)
            return false;
        try {
            JSONObject jso = new JSONObject(response);
            return jso.isNull("error");
        } catch (JSONException e) {
            return false;
        }

    }

    /*
    "400 Bad Request" is used when HTTP request has wrong format or some request parameters are missing or have invalid type (i.e. contain letters where numbers are expected).
"404 Path Not Found" is used to indicate invalid request name.
"405 Method Not Allowed" is used when method name is other than POST.
"411 Length Required" is used if Content-Length request header is missing.
"413 Request Entity Too Large" is used when request body size is more than 10000 bytes (it's more than enough for all implemented requests).
"415 Unsupported Media Type" is used when content type is not application/x-www-form-urlencoded.
     */

    @Override
    protected Exception parseException(String responceStr, int code) {
        switch (code) {
            case 400:
                return new ServerException(code, "Bad Request");
            case 404:
                return new ServerException(code, "Path Not Found");
            case 405:
                return new ServerException(code, "Method Not Allowed");
            case 411:
                return new ServerException(code, "Length Required");
            case 413:
                return new ServerException(code, "Request Entity Too Large");
            case 415:
                return new ServerException(code, "Unsupported Media Type");

            case 200:
                try {
                    JSONObject jso = new JSONObject(responceStr);
                    if (!jso.isNull("error")) {
                        JSONObject error = jso.getJSONObject("error");
                        int errCode = error.getInt("code");
                        String message;
                        if (!error.isNull("data") && !error.getJSONObject("data").isNull("message"))
                            message = error.getJSONObject("data").getString("message");
                        else message = error.optString("message");
                        return new ServerException(errCode, message);
                    }
                } catch (JSONException e) {
                    return e;
                }

        }
        return new ServerException(code, "Unknown exception");
    }
}