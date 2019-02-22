package io.prover.clapperboard.enterprise.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.prover.clapperboard.enterprise.R;
import io.prover.clapperboard.enterprise.Settings;
import io.prover.common.enterprise.auth.AuthPageType;

public class ClapperboardSettingsFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_PAGE = "page";

    private ClapperboardSettingsPage page;
    private TextView selectedResolutionText;

    public ClapperboardSettingsFragment() {
        // Required empty public constructor
    }

    public static Fragment instantiate(ClapperboardSettingsPage page) {
        ClapperboardSettingsFragment fragment = new ClapperboardSettingsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_PAGE))
            page = args.getParcelable(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_settings, container, false);

        root.findViewById(R.id.howToUseControl).setOnClickListener(this);
        root.findViewById(R.id.changeServerAddress).setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.howToUseControl:
                Activity activity = getActivity();
                if (activity != null)
                    activity.finish();
                startActivity(Settings.authActivityIntent(v.getContext(), AuthPageType.Help));
                break;

            case R.id.changeServerAddress:
                activity = getActivity();
                if (activity != null)
                    activity.finish();
                startActivity(Settings.authActivityIntent(v.getContext(), AuthPageType.Login));
                break;
        }
    }
}
