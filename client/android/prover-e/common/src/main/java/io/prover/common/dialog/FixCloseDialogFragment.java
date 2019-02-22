package io.prover.common.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Created with IntelliJ IDEA.
 * User: babay
 * Date: 29.11.13
 * Time: 9:10
 */
public abstract class FixCloseDialogFragment extends DialogFragment implements DialogInterface {
    protected Handler handler = new Handler();
    OnDismissListener onDismissListener;
    private boolean closeMe;
    private boolean noBack;

    //mStackLevel++;
    // DialogFragment.show() will take care of adding the fragment
    // in a transaction.  We also want to remove any currently showing
    // dialog, so make our own transaction and take care of that here.
    public boolean show(FragmentManager fm) {
        final FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        try {
            show(ft, "dialog");
        } catch (IllegalStateException e) {
            Log.e("FixCloseDialogFragment", e.getMessage(), e);
            return false;
        }
        return true;
    }

    protected void dismissNoBack() {
        try {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(this);
            ft.commit();
            if (onDismissListener != null)
                onDismissListener.onDismiss(this);
        } catch (Exception e) {
            closeMe = true;
            noBack = true;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dlg = super.onCreateDialog(savedInstanceState);
        int dividerId = dlg.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        View divider = dlg.findViewById(dividerId);
        if (divider != null) {
            divider.setVisibility(View.GONE);
        }
        return dlg;
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void cancel() {
        dismiss();
    }

    protected void checkCloseMe() {
        if (closeMe) {
            closeMe = false;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (noBack)
                        dismissNoBack();
                    else
                        dismiss();
                }
            });
        }
    }

    protected void setCloseMe() {
        closeMe = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkCloseMe();
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
            if (onDismissListener != null)
                onDismissListener.onDismiss(this);
        } catch (Exception e) {
            closeMe = true;
        }
    }

    protected void setFullHeightDialog() {
        //getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
        p.width = ViewGroup.LayoutParams.MATCH_PARENT;
        p.height = ViewGroup.LayoutParams.MATCH_PARENT;
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
        getDialog().getWindow().setAttributes(p);
    }
}
