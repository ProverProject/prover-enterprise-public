package io.prover.common.pages.base;

import android.content.Context;

/**
 * Created by babay on 05.09.2017.
 */

public interface IPageFragment<P extends IPage> {
    P getPage();

    String getTitle(Context context);
}
