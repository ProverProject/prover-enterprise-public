package io.prover.swypeid.enterprise.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import io.prover.common.enterprise.auth.AuthPageType;
import io.prover.swypeid.camera2.Size;
import io.prover.swypeid.enterprise.R;
import io.prover.swypeid.enterprise.Settings;

import static io.prover.swypeid.enterprise.Const.KEY_SELECTED_RESOLUTION_X;
import static io.prover.swypeid.enterprise.Const.KEY_SELECTED_RESOLUTION_Y;
import static io.prover.swypeid.enterprise.Const.KEY_SHOW_FPS;
import static io.prover.swypeid.enterprise.Const.KEY_USE_FAST_SWYPECODE;

public class SwypeIdSettingsFragment extends Fragment {

    private static final String ARG_PAGE = "page";

    private SwypeIdSettingsPage page;
    private TextView selectedResolutionText;

    public SwypeIdSettingsFragment() {
        // Required empty public constructor
    }

    public static Fragment instantiate(SwypeIdSettingsPage page) {
        SwypeIdSettingsFragment fragment = new SwypeIdSettingsFragment();
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(inflater.getContext());

        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_settings, container, false);

        Switch showFpsSwitch = root.findViewById(R.id.showFpsSwitch);
        Switch fastSwypeCodeSwitch = root.findViewById(R.id.fastSwypeCodeSwitch);

        selectedResolutionText = root.findViewById(R.id.videoResolutionView);
        root.findViewById(R.id.resolutionControl).setOnClickListener(this::onClick);
        root.findViewById(R.id.showFpsControl).setOnClickListener(this::onClick);
        root.findViewById(R.id.howToUseControl).setOnClickListener(this::onClick);
        root.findViewById(R.id.fastSwypeCodeControl).setOnClickListener(this::onClick);
        root.findViewById(R.id.changeServerAddress).setOnClickListener(this::onClick);

        showFpsSwitch.setChecked(prefs.getBoolean(KEY_SHOW_FPS, false));
        fastSwypeCodeSwitch.setChecked(prefs.getBoolean(KEY_USE_FAST_SWYPECODE, true));
        showFpsSwitch.setOnCheckedChangeListener(this::onCheckedChanged);
        fastSwypeCodeSwitch.setOnCheckedChangeListener(this::onCheckedChanged);

        Size resolution = Size.fromPreferences(prefs, KEY_SELECTED_RESOLUTION_X, KEY_SELECTED_RESOLUTION_Y);
        if (resolution != null) {
            selectedResolutionText.setText(resolution.toString());
        }

        return root;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.resolutionControl:
                showSelectResolutionAlertDialog(v);
                break;

            case R.id.showFpsControl:
                ((Switch) v.findViewById(R.id.showFpsSwitch)).toggle();
                break;

            case R.id.howToUseControl:
                Activity activity = getActivity();
                if (activity != null)
                    activity.finish();
                startActivity(Settings.authActivityIntent(v.getContext(), AuthPageType.Help));
                break;

            case R.id.fastSwypeCodeControl:
                ((Switch) v.findViewById(R.id.fastSwypeCodeSwitch)).toggle();
                break;

            case R.id.changeServerAddress:
                activity = getActivity();
                if (activity != null)
                    activity.finish();
                startActivity(Settings.authActivityIntent(v.getContext(), AuthPageType.Login));
                break;
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.showFpsSwitch:
                PreferenceManager.getDefaultSharedPreferences(buttonView.getContext())
                        .edit()
                        .putBoolean(KEY_SHOW_FPS, isChecked)
                        .apply();
                break;

            case R.id.fastSwypeCodeSwitch:
                PreferenceManager.getDefaultSharedPreferences(buttonView.getContext())
                        .edit()
                        .putBoolean(KEY_USE_FAST_SWYPECODE, isChecked)
                        .apply();
                break;
        }
    }

    private void showSelectResolutionAlertDialog(View v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
        Size selected = Size.fromPreferences(prefs, KEY_SELECTED_RESOLUTION_X, KEY_SELECTED_RESOLUTION_Y);
        int selectedPos = -1;

        String[] sizes = new String[page.sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            Size size = page.sizes[i];
            sizes[i] = size.toString();
            if (selected != null && size.width == selected.width && size.height == selected.height) {
                selectedPos = i;
            }
        }

        new AlertDialog.Builder(v.getContext())
                .setTitle(R.string.select_video_resolution)
                .setSingleChoiceItems(sizes, selectedPos, (dialog, which) -> {
                    dialog.dismiss();
                    Size size = page.sizes[which];
                    prefs.edit().putInt(KEY_SELECTED_RESOLUTION_X, size.width)
                            .putInt(KEY_SELECTED_RESOLUTION_Y, size.height).apply();
                    selectedResolutionText.setText(size.toString());
                })
                .show();
    }
}
