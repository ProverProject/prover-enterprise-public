package io.prover.swypeid.templates;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

import static io.prover.common.Const.TAG;

public class TemplateLoader extends AsyncTask<Object, Object, Template> {
    private final Context context;
    private final TemplateLoadedCallback callback;

    private final String templateId;
    private volatile OnErrorCallback errorCallback;

    public TemplateLoader(Context context, String templateId, TemplateLoadedCallback callback) {
        this.context = context.getApplicationContext();
        this.callback = callback;
        this.templateId = templateId;
    }

    @Override
    protected Template doInBackground(Object... objects) {
        try {
            return Template.open(context, templateId);
        } catch (JSONException | IOException e) {
            Log.e(TAG, "doInBackground: ", e);
            OnErrorCallback cb = errorCallback;
            if (cb != null)
                cb.onError(e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Template template) {
        callback.onTemplateLoaded(template);
    }

    public TemplateLoader onError(OnErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
        return this;
    }

    public interface TemplateLoadedCallback {
        void onTemplateLoaded(@Nullable Template template);
    }

    public interface OnErrorCallback {
        void onError(Exception e);
    }
}