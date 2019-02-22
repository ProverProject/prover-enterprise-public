package io.prover.common.transport.base;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.prover.common.BuildConfig;
import okhttp3.FormBody;
import okhttp3.Request;
import okio.Buffer;

import static io.prover.common.transport.base.NetworkRequest.TAG;


/**
 * Created by babay on 07.09.2017.
 */

public class RequestLog {
    private static final int MAX_ERROR_LENGTH = 128;
    private static final boolean FULL_ERROR_RESPONCE = false;
    private final RequestType requestType;
    private boolean storeDebug = true;
    private String url;
    private String requestBody;
    private Exception exception;
    private long createTime;
    private long requestStartTime;
    private long requestEndTime;
    private int responceCode;
    private HashMap<String, String> headers;
    private HashMap<String, String> fields;
    private String responce;
    private boolean jsonParcelable;

    public RequestLog(RequestType requestType) {
        this.requestType = requestType;
        createTime = System.currentTimeMillis();
    }

    public void onStart(HttpURLConnection urlConnection) {
        if (storeDebug) {
            url = urlConnection.getURL().toString();
            Map<String, List<String>> props = urlConnection.getRequestProperties();
            headers = new HashMap<>();
            StringBuilder builder = new StringBuilder();
            for (String key : props.keySet()) {
                builder.replace(0, builder.length(), "");
                List<String> values = props.get(key);
                for (String value : values) {
                    if (builder.length() > 0)
                        builder.append(", ");
                    builder.append(value);
                }
                headers.put(key, builder.toString());
            }
            Log.d(TAG, "sending request" + toString());
        }
        requestStartTime = System.currentTimeMillis();
    }

    public void onStart(Request request) {
        if (storeDebug) {

            if (request.body() != null) {
                Buffer sink = new Buffer();
                try {
                    request.body().writeTo(sink);
                    requestBody = sink.readUtf8();
                    if (request.body() instanceof FormBody) {
                        requestBody = requestBody.replaceAll("&", "\n&");
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }

            url = request.url().toString();
            Map<String, List<String>> props = request.headers().toMultimap();
            headers = new HashMap<>();
            StringBuilder builder = new StringBuilder();
            for (String key : props.keySet()) {
                builder.replace(0, builder.length(), "");
                List<String> values = props.get(key);
                for (String value : values) {
                    if (builder.length() > 0)
                        builder.append(", ");
                    builder.append(value);
                }
                headers.put(key, builder.toString());
            }
        }
        requestStartTime = System.currentTimeMillis();
    }

    public void onGotResponse(String result, int code) {
        requestEndTime = System.currentTimeMillis();
        if (storeDebug) {
            responce = result;
            responceCode = code;
        }
        try {
            new JSONObject(result);
            jsonParcelable = true;
        } catch (JSONException e) {
            jsonParcelable = false;
        }
    }

    public void appendFormField(String name, String value) {
        if (fields == null)
            fields = new HashMap<>();
        fields.put(name, value);
    }

    public void appendFile(String fieldName, File uploadFile) {
        if (fields == null)
            fields = new HashMap<>();
        fields.put(fieldName, "file: " + uploadFile.getPath());
    }

    public RequestLog setException(Exception e) {
        exception = e;
        return this;
    }

    public void log() {
        if (exception == null) {
            Log.d(TAG, toString());
        } else {
            Log.e(TAG, toString());
            Log.e(TAG, exception.getMessage(), exception);
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "curl request: " + toCurlString());
        }
    }

    @Override
    public String toString() {
        return toStringBuilder().toString();
    }

    private StringBuilder toStringBuilder() {
        StringBuilder builder = new StringBuilder();
        builder.append(requestType.name()).append(" request: ").append(url).append("\n");
        if (headers != null && headers.size() > 0) {
            for (String key : headers.keySet()) {
                builder.append(key).append(": ").append(headers.get(key)).append("\n");
            }
        }
        if (fields != null && fields.size() > 0) {
            for (String key : fields.keySet()) {
                builder.append(key).append(": ").append(fields.get(key)).append("\n");
            }
        }
        if (requestBody != null)
            builder.append("request: ").append(requestBody).append("\n");
        if (requestEndTime != 0) {
            builder.append("response time: ").append(requestEndTime - requestStartTime)
                    .append("; code: ").append(responceCode).append(";\n");
            if (jsonParcelable || FULL_ERROR_RESPONCE || exception == null || responce.length() < MAX_ERROR_LENGTH)
                builder.append("response: ").append(responce).append("\n");
            else {
                builder.append("response: ").append(responce.substring(0, MAX_ERROR_LENGTH)).append("\n");
            }
        }
        return builder;
    }

    public String toCurlString() {
        StringBuilder builder = new StringBuilder(" ");
        if (requestBody != null) {
            builder.append(" -d \"")
                    .append(requestBody.replaceAll("\n&", "&"))
                    .append('\"');
        }

        if (headers != null) {
            for (String key : headers.keySet()) {
                String value = headers.get(key);
                builder.append(" -H \"")
                        .append(key)
                        .append(": ")
                        .append(value)
                        .append('\"');
            }
        }

        builder.append(" -X ").append(requestType.name().toUpperCase())
                .append(' ')
                .append(url);

        return builder.toString();
    }

    public void logToFile(File file) {
        StringBuilder builder = toStringBuilder();
        if (exception != null) {
            builder.append(Log.getStackTraceString(exception));
            builder.append(TextUtils.join("\n", exception.getStackTrace()));
        }

        try (FileOutputStream stream = new FileOutputStream(file, true)) {
            stream.write("\n\n".getBytes());
            stream.write(builder.toString().getBytes());
            stream.write("\n\n".getBytes());
            stream.flush();
            stream.close();
        } catch (IOException e) {
            Log.e("error", e.getMessage(), e);
        }
    }

}
