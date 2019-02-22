package io.prover.swypeid.templates;

import android.graphics.drawable.Drawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TranslatedTemplate {
    private static final DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd-MM-YYYY");
    public final String languageStr;
    public final Locale locale;
    public final String title;
    public final String author;
    public final String contact;
    public final LocalDate date;
    public final List<String> participants;
    public final List<String> requirements;
    public final List<TemplateStep> steps;
    public final Drawable icon;

    /*
    "language" : "EN",
            "title" : "Sales agreement",
            "author" : "Ivan Pisarev"
            "contact" : "info@prover.io",
            "date" : "27-01-2019",
            "participants" :
            [
                "buyer",
                "seller"
            ],
            "requirements" : [
                "buyer's passport",
                "seller's passport"
            ],
            "steps" :
            [
                {
     */

    public TranslatedTemplate(JSONObject source, Drawable icon) throws JSONException {
        this.icon = icon;
        languageStr = source.optString("language");
        locale = languageStr == null ? null : new Locale(languageStr.toLowerCase());
        title = source.getString("title");
        author = source.optString("author");
        contact = source.optString("contact");

        String dateStr = source.optString("date");
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, pattern);
        } catch (Exception e) {
            date = null;
        }
        this.date = date;
        participants = parseStrings(source.getJSONArray("participants"));
        requirements = parseStrings(source.getJSONArray("requirements"));
        steps = parseSteps(source.getJSONArray("steps"));
    }

    private static List<String> parseStrings(JSONArray src) throws JSONException {
        List<String> result = new ArrayList<>(src.length());
        for (int i = 0; i < src.length(); ++i) {
            result.add(src.getString(i));
        }
        return Collections.unmodifiableList(result);
    }

    private static List<TemplateStep> parseSteps(JSONArray src) throws JSONException {
        List<TemplateStep> result = new ArrayList<>(src.length());
        for (int i = 0; i < src.length(); ++i) {
            result.add(new TemplateStep(src.getJSONObject(i)));
        }
        return Collections.unmodifiableList(result);
    }
}