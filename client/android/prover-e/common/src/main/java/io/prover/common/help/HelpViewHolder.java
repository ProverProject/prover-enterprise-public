package io.prover.common.help;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.prover.common.R;

public class HelpViewHolder {

    public final ViewGroup root;

    private HelpViewHolder(ViewGroup viewGroup, int imageId, int textId) {
        root = viewGroup;
        ImageView imageView = root.findViewById(R.id.helpImage);
        TextView textView = root.findViewById(R.id.helpText);
        imageView.setImageResource(imageId);
        textView.setText(textId);
    }

    public static HelpViewHolder inflate(ViewGroup parent, int imageId, int textId, boolean attach) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.view_help, parent, false);
        if (attach) {
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            parent.addView(v, lp);
        }
        return new HelpViewHolder(v, imageId, textId);
    }
}
