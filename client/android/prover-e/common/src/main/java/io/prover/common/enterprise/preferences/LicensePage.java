package io.prover.common.enterprise.preferences;

import android.content.res.Resources;
import android.os.Parcel;
import android.support.v4.app.Fragment;

import io.prover.common.R;
import io.prover.common.prefs.IPreferencesPage;

public class LicensePage implements IPreferencesPage {

    public static final Creator<LicensePage> CREATOR = new Creator<LicensePage>() {
        @Override
        public LicensePage createFromParcel(Parcel source) {
            return new LicensePage();
        }

        @Override
        public LicensePage[] newArray(int size) {
            return new LicensePage[0];
        }
    };

    public LicensePage() {
    }

    @Override
    public Fragment makeFragment() {
        return new LicenseFragment();
    }

    @Override
    public String getPageTitle(Resources resources) {
        return resources.getString(R.string.license);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
