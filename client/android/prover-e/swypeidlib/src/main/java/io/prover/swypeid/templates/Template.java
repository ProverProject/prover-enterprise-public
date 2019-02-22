package io.prover.swypeid.templates;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.prover.common.util.Base64;
import io.prover.common.util.UtilFile;
import io.prover.swypeid.util.SvgDrawable;

import static io.prover.common.Const.TAG;

public class Template {
    @NonNull
    public final List<TranslatedTemplate> templates;

    public final String filePath;

    /**
     * true if the template stored in assets and should accessed with assets.
     * false if the template is stored as a file in file system
     */
    public final boolean isAssets;
    /**
     * string id of a Template. Contains {@link #isAssets} and {@link #filePath}
     */
    public final String templateId;
    public final @CameraPreference
    int cameraPreference;
    @Nullable
    private final Drawable drawable;

    public Template(JSONObject source, Resources res, String filePath, boolean isAssets) throws JSONException {
        this.filePath = filePath;
        this.isAssets = isAssets;
        String iconString = source.optString("icon");
        drawable = iconString != null ? parseDrawable(iconString, res) : null;
        templates = parseTemplates(source.getJSONArray("agreementTemplate"), drawable);

        String cameraPref = source.optString("camera");
        if ("back".equalsIgnoreCase(cameraPref))
            cameraPreference = CameraPreference.BACK;
        else if ("front".equalsIgnoreCase(cameraPref))
            cameraPreference = CameraPreference.FRONT;
        else
            cameraPreference = CameraPreference.UNSPECIFIED;

        templateId = (isAssets ? "1" : "0") + ":" + filePath;
    }

    @Nullable
    public static Template open(Context context, String templateId) throws IOException, JSONException {
        boolean isAssets = templateId.charAt(0) == '1';
        String fileName = templateId.substring(2);
        return open(context, fileName, isAssets);
    }

    @Nullable
    public static Template open(Context context, String fileName, boolean isAssets) throws IOException, JSONException {
        String source;
        try (InputStream is = isAssets ? context.getAssets().open(fileName) : new FileInputStream(fileName)) {
            source = UtilFile.readFully(is);
        }
        return new Template(new JSONObject(source), context.getResources(), fileName, isAssets);
    }

    public static Template safeOpen(Context context, String fileName, boolean isAssets) {
        try {
            return open(context, fileName, isAssets);
        } catch (JSONException | IOException e) {
            Log.e(TAG, "safeOpen: ", e);
            return null;
        }
    }

    private static Drawable parseDrawable(String src, Resources res) {
        byte[] bytes = Base64.decode(src);
        if (bytes[0] == '<') {
            return SvgDrawable.fromStringBytes(bytes, res, 24, 24);
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        bitmap.setDensity(640);
        Drawable dr = new BitmapDrawable(res, bitmap);
        dr.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        return dr;
    }

    private static List<TranslatedTemplate> parseTemplates(JSONArray src, Drawable drawable) throws JSONException {
        List<TranslatedTemplate> result = new ArrayList<>(src.length());
        for (int i = 0; i < src.length(); ++i) {
            result.add(new TranslatedTemplate(src.getJSONObject(i), drawable));
        }
        return Collections.unmodifiableList(result);
    }

    @NonNull
    static List<Template> loadTemplatesFromAssets(Context context) {
        List<Template> result = new ArrayList<>();
        String parentDir = "templates";
        try {
            String[] fileNames = context.getAssets().list(parentDir);
            if (fileNames != null) {
                for (String fileName : fileNames) {
                    String path = parentDir + File.separatorChar + fileName;
                    Template template = Template.safeOpen(context, path, true);
                    if (template != null)
                        result.add(template);
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "loadTemplatesFromAssets: ", e);
        }
        return result;
    }

    public TranslatedTemplate templateForCurrentLocale(Resources res) {
        Locale currentLocale = res.getConfiguration().locale;
        for (TranslatedTemplate template : templates) {
            if (currentLocale.getLanguage().equals(template.locale.getLanguage()))
                return template;
        }
        for (TranslatedTemplate template : templates) {
            if ("en".equals(template.locale.getLanguage()))
                return template;
        }
        return templates.get(0);
    }

    @Nullable
    public Drawable getDrawable() {
        return drawable;
    }

    @IntDef({
            CameraPreference.UNSPECIFIED,
            CameraPreference.BACK,
            CameraPreference.FRONT})
    @Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraPreference {
        int UNSPECIFIED = 0;
        int BACK = 1;
        int FRONT = 2;
    }
}