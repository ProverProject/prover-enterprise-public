package io.prover.common.pages.base;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public abstract class PageController<E extends Enum<E>, P extends PageBase<E>> {

    protected final AppCompatActivity activity;
    private final int fragmentContainerId;
    private final Handler handler = new Handler();

    public PageController(AppCompatActivity activity, int fragmentContainerId) {
        this.activity = activity;
        this.fragmentContainerId = fragmentContainerId;
    }

    private Fragment getCurrentFragment() {
        return activity.getSupportFragmentManager().findFragmentById(fragmentContainerId);
    }

    public P getCurrentPage() {
        Fragment f = getCurrentFragment();
        return f instanceof IPageFragment ? ((IPageFragment<P>) f).getPage() : null;
    }

    /**
     * @param intent
     * @return page stored in intent; null if intent has no page
     */
    public P parseIntent(Intent intent) {
        if (intent.hasExtra(PageBase.PAGE_ARG_NAME)) {
            return intent.getParcelableExtra(PageBase.PAGE_ARG_NAME);
        }
        return null;
    }

    public void showPage(@NonNull final P page) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            handler.post(() -> showPage(page));
            return;
        }

        P oldPage = getCurrentPage();
        if (page.equals(oldPage)) {
            return;
        }
        activity.setIntent(page.intent(activity));
        beforeShowPage(page);
        if (tryShowDialog(page))
            return;

        Fragment f = createFragment(page);
        if (f != null) {
            FragmentTransaction fTrans = activity.getSupportFragmentManager().beginTransaction();

            applyAnimations(fTrans);

            if (shouldClearBackstack(oldPage, page))
                closeAllFragments();

            fTrans.replace(fragmentContainerId, f).setReorderingAllowed(true);
            if (shouldAddToBackstack(oldPage, page))
                fTrans.addToBackStack(null);
            try {
                fTrans.commit();
            } catch (Throwable ignored) {
            }
        }
    }

    private void closeAllFragments() {
        try {
            activity.getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } catch (NullPointerException ignored) {
        }
    }

    protected void beforeShowPage(final P page) {
    }

    protected boolean tryShowDialog(final P page) {
        return false;
    }

    protected void applyAnimations(FragmentTransaction fTrans) {
        /*if (activity.getSupportFragmentManager().getBackStackEntryCount() > 0)
                fTrans.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);*/
    }

    protected Fragment createFragment(P page) {
        return page.makeFragment();
    }

    @NonNull
    public abstract P getDefaultPage();

    protected abstract boolean shouldAddToBackstack(@Nullable P oldPage, @NonNull P page);

    protected abstract boolean shouldClearBackstack(@Nullable P oldPage, @NonNull P page);
}
