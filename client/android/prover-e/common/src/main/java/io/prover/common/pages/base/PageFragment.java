package io.prover.common.pages.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class PageFragment<P extends IPage> extends Fragment implements IPageFragment<P> {

    protected static final String ARG_PAGE = "page";
    protected IFragmentInteractionListener<P> mListener;
    protected P page;
    protected boolean resumed = false;

    public PageFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            page = args.getParcelable(ARG_PAGE);
        }
    }

    @Override
    public P getPage() {
        return page;
    }

    @Override
    public String getTitle(Context context) {
        return page.getType().name();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IFragmentInteractionListener) {
            mListener = (IFragmentInteractionListener<P>) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        resumed = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        resumed = false;
    }

    protected void showPage(P page, Context context) {
        if (mListener != null)
            mListener.showPage(page);
        else {
            Intent intent = page.intent(context);
            if (intent != null) {
                context.startActivity(intent);
            }
        }
    }
}
