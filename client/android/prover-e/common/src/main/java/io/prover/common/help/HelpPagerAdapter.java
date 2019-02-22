package io.prover.common.help;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

class HelpPagerAdapter extends PagerAdapter {

    private int[] helpImages;
    private int[] helpStrings;

    HelpPagerAdapter(IHelpPage page, Context context) {
        helpStrings = loadIntArray(context, page.getHelpStringIds());
        helpImages = loadIntArray(context, page.getHelpImageIds());
    }

    private static int[] loadIntArray(Context context, int arrayId) {
        TypedArray ar = context.getResources().obtainTypedArray(arrayId);
        int len = ar.length();
        int[] resIds = new int[len];
        for (int i = 0; i < len; i++)
            resIds[i] = ar.getResourceId(i, 0);
        ar.recycle();
        return resIds;
    }

    @Override
    public int getCount() {
        return helpImages.length;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return HelpViewHolder.inflate(container, helpImages[position], helpStrings[position], true);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object instanceof HelpViewHolder && ((HelpViewHolder) object).root == view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (object instanceof HelpViewHolder) {
            container.removeView(((HelpViewHolder) object).root);
        }
    }
}
