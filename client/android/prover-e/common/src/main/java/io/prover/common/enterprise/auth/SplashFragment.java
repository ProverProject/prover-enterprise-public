package io.prover.common.enterprise.auth;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import io.prover.common.R;
import io.prover.common.pages.base.PageFragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AuthFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SplashFragment extends PageFragment<AuthPage> {

    public SplashFragment() {
    }

    public static SplashFragment newInstance(AuthPage page) {
        SplashFragment f = new SplashFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PAGE, page);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_splash, container, false);
        ImageView v = root.findViewById(R.id.logoSubtitle);
        if (page.subtitleDrawableId != 0)
            v.setImageResource(page.subtitleDrawableId);

        ImageView titleView = root.findViewById(R.id.logoTitle);
        titleView.setImageResource(R.drawable.ic_prover_enterprise);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(() -> {
            Activity activity = getActivity();
            if (resumed && activity != null) {
                mListener.showPage(null);
            }
        }, 1500);
    }
}