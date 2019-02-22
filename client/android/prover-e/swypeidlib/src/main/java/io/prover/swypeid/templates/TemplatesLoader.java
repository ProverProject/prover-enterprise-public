package io.prover.swypeid.templates;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

public class TemplatesLoader extends AsyncTask<Object, Object, List<Template>> {
    private final Context context;
    private final TemplatesLoadedCallback callback;

    public TemplatesLoader(Context context, TemplatesLoadedCallback callback) {
        this.context = context.getApplicationContext();
        this.callback = callback;
    }

    @Override
    protected List<Template> doInBackground(Object... objects) {
        return Template.loadTemplatesFromAssets(context);
    }

    @Override
    protected void onPostExecute(List<Template> templates) {
        callback.onTemplatesLoaded(templates);
    }

    public interface TemplatesLoadedCallback {
        void onTemplatesLoaded(List<Template> templates);
    }
}