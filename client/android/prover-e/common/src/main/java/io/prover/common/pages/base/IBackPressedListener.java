package io.prover.common.pages.base;

/**
 * Created by babay on 01.09.2017.
 */

public interface IBackPressedListener {
    /**
     * @return true if click is consumed
     */
    boolean handleBackPressed();
}
