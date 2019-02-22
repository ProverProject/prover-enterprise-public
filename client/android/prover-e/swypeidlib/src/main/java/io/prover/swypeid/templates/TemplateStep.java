package io.prover.swypeid.templates;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateStep {
    private static final String STEP_FORMAT_REGEX = "(\\+[^\\+]*\\+|_[^_]*_)";

    @NonNull
    public final Type type;
    @Nullable
    public final String participant;
    @NonNull
    public final String text;

    public TemplateStep(JSONObject source) throws JSONException {
        participant = source.optString("participant");
        text = source.getString("text");

        String type = source.optString("type");
        this.type = Type.fromString(type);
    }

    /*
      "type" : "action",
      "participant" : "seller",
      "text" : "_Seller_, name your full name, and your job title."
     */

    public SpannableStringBuilder formattedStepText(Resources res) {
        StringBuffer buf = new StringBuffer(text.length());
        SpannableStringBuilder spanned = new SpannableStringBuilder();
        //matches +text+ or _text_
        Pattern pattern = Pattern.compile(STEP_FORMAT_REGEX);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String group = matcher.group();
            String spanText = group.substring(1, group.length() - 1);
            buf.setLength(0);
            matcher.appendReplacement(buf, spanText);
            spanned.append(buf.toString());

            int start = spanned.length() - spanText.length();
            if (group.charAt(0) == '+')
                addParticipantSpan(spanned, start);
            else
                addRequirementSpan(spanned, start);
        }
        buf.setLength(0);
        matcher.appendTail(buf);
        spanned.append(buf.toString());

        return spanned;
    }

    private void addParticipantSpan(SpannableStringBuilder spanned, int start) {
        spanned.setSpan(new StyleSpan(Typeface.BOLD), start, spanned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void addRequirementSpan(SpannableStringBuilder spanned, int start) {
        spanned.setSpan(new StyleSpan(Typeface.BOLD), start, spanned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public enum Type {
        Action, Info, CameraSelection, Unknown;

        public static Type fromString(String source) {
            source = source.toLowerCase();
            if (Action.name().toLowerCase().equals(source))
                return Action;
            if (Info.name().toLowerCase().equals(source))
                return Info;
            if (CameraSelection.name().toLowerCase().equals(source))
                return CameraSelection;

            return Unknown;
        }
    }

}