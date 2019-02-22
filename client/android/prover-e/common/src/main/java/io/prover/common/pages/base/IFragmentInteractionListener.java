package io.prover.common.pages.base;


import android.support.annotation.Nullable;

/**
 * Created by babay on 30.01.2017.
 */

public interface IFragmentInteractionListener<P extends IPage> {
    void showPage(@Nullable P page);

    void closeFragment();
}
