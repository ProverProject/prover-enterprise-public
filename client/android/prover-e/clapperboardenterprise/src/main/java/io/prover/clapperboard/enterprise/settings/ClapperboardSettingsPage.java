package io.prover.clapperboard.enterprise.settings;

import android.content.res.Resources;
import android.os.Parcel;
import android.support.v4.app.Fragment;

import io.prover.common.prefs.IPreferencesPage;

public class ClapperboardSettingsPage implements IPreferencesPage {

    public static final Creator<ClapperboardSettingsPage> CREATOR = new Creator<ClapperboardSettingsPage>() {
        @Override
        public ClapperboardSettingsPage createFromParcel(Parcel source) {
            return new ClapperboardSettingsPage();
        }

        @Override
        public ClapperboardSettingsPage[] newArray(int size) {
            return new ClapperboardSettingsPage[size];
        }
    };

    @Override
    public Fragment makeFragment() {
        return ClapperboardSettingsFragment.instantiate(this);
    }

    @Override
    public String getPageTitle(Resources resources) {
        return resources.getString(io.prover.common.R.string.settings);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
