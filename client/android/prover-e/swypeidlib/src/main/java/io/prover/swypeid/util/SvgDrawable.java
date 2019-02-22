package io.prover.swypeid.util;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Picture;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.nio.charset.Charset;

import static io.prover.common.Const.TAG;

public class SvgDrawable extends Drawable {

    private final SVG svg;
    private final float scale;
    private Drawable drawable;

    public SvgDrawable(SVG svg, float scale) {
        this.svg = svg;
        this.scale = scale;
        svg.setRenderDPI(160 * scale);
        try {
            svg.setDocumentWidth("100%");
            svg.setDocumentHeight("100%");
        } catch (Exception e) {
            Log.e(TAG, "SvgDrawable: ", e);
        }
    }

    public static SvgDrawable fromStringBytes(byte[] bytes, Resources res, int widthDp, int heightDp) {
        String str = new String(bytes, Charset.forName("UTF-8"));
        try {
            SVG svg = SVG.getFromString(str);
            int width = (int) (widthDp * res.getDisplayMetrics().density);
            int height = (int) (heightDp * res.getDisplayMetrics().density);
            SvgDrawable dr = new SvgDrawable(svg, res.getDisplayMetrics().density);
            dr.setBounds(0, 0, width, height);
            return dr;
        } catch (SVGParseException e) {
            Log.e(TAG, "parseDrawable: ", e);
            return null;
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (drawable == null)
            drawable = createPictureDrawable();

        canvas.save();
        drawable.draw(canvas);
        canvas.restore();

    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        //if (drawable != null)
        //    drawable.setBounds(left, top, right, bottom);
        super.setBounds(left, top, right, bottom);
    }

    @Override
    public void setBounds(@NonNull Rect bounds) {
        //if (drawable != null)
        //    drawable.setBounds(bounds);
        super.setBounds(bounds);
    }

    @Override
    public void setAlpha(int alpha) {
        if (drawable == null)
            drawable = createPictureDrawable();
        drawable.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        if (drawable == null)
            drawable = createPictureDrawable();
        drawable.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private Drawable createPictureDrawable() {
        Rect b = getBounds();
        Picture p = svg.renderToPicture(b.width(), b.height());
        PictureDrawable pd = new PictureDrawable(p);
        pd.setBounds(0, 0, p.getWidth(), p.getHeight());
        return pd;
    }

    /*private Drawable createBitmapDrawable() {
        Bitmap bm = Bitmap.createBitmap((int) svg.getDocumentWidth(), (int) svg.getDocumentHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        svg.renderToCanvas(c);
        BitmapDrawable dr =new BitmapDrawable(bm);
        dr.setBounds();
        return pd;
    }*/


    public SVG getSvg() {
        return svg;
    }
}
