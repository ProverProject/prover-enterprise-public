package io.prover.common.prefs;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class WalletAndPreferencesPagerAdapter extends FragmentPagerAdapter {
    private final Context context;
    private final IPreferencesPage[] pages;

    WalletAndPreferencesPagerAdapter(Context context, FragmentManager fragmentManager, IPreferencesPage[] pages) {
        super(fragmentManager);
        this.context = context;
        this.pages = pages;
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return pages.length;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        return pages[position].makeFragment();
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return pages[position].getPageTitle(context.getResources());
    }
}