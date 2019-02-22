package io.prover.common.help;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import io.prover.common.R;
import io.prover.common.pages.base.PageFragment;

import static io.prover.common.Const.ARG_TUTORIAL_SHOWN;

public class HelpFragment extends PageFragment<IHelpPage> {

    public static HelpFragment newInstance(IHelpPage page) {
        HelpFragment f = new HelpFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PAGE, page);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_help, container, false);

        ImageView titleView = root.findViewById(R.id.logoTitle);
        ImageView subtitleView = root.findViewById(R.id.logoSubtitle);
        if (page.getTitleDrawableId() != 0)
            titleView.setImageResource(page.getTitleDrawableId());
        if (page.getSubtitleDrawableId() != 0)
            subtitleView.setImageResource(page.getSubtitleDrawableId());

        ViewPager pager = root.findViewById(R.id.vpPager);
        PagerAdapter adapter = new HelpPagerAdapter(page, root.getContext());
        pager.setAdapter(adapter);
        Button skipButton = root.findViewById(R.id.skipButton);
        skipButton.setOnClickListener(v1 -> {
            PreferenceManager.getDefaultSharedPreferences(root.getContext()).edit().putBoolean(ARG_TUTORIAL_SHOWN, true).apply();
            mListener.closeFragment();
        });

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                skipButton.setText(position == adapter.getCount() - 1 ? R.string.finish : R.string.skip);
            }
        });
        return root;
    }
}
