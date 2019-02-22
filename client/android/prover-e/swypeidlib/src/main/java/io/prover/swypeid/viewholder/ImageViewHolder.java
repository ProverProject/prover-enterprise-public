package io.prover.swypeid.viewholder;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.widget.ImageView;

/**
 * Created by babay on 10.12.2017.
 */

public class ImageViewHolder {
    public final ImageView imageView;

    public ImageViewHolder(ImageView imageView) {
        this.imageView = imageView;
    }

    protected void setDrawable(int id) {
        Drawable dr = AppCompatResources.getDrawable(imageView.getContext(), id);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        imageView.setImageDrawable(dr);
        if (dr instanceof Animatable) {
            ((Animatable) dr).start();
        }
    }
}
