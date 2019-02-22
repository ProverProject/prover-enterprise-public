package io.prover.swypeid.enterprise.settings;

import android.content.res.Resources;
import android.os.Parcel;
import android.support.v4.app.Fragment;

import java.util.List;

import io.prover.common.prefs.IPreferencesPage;
import io.prover.swypeid.camera2.Size;

public class SwypeIdSettingsPage implements IPreferencesPage {

    public static final Creator<SwypeIdSettingsPage> CREATOR = new Creator<SwypeIdSettingsPage>() {
        @Override
        public SwypeIdSettingsPage createFromParcel(Parcel source) {
            return new SwypeIdSettingsPage(source);
        }

        @Override
        public SwypeIdSettingsPage[] newArray(int size) {
            return new SwypeIdSettingsPage[0];
        }
    };

    public final Size[] sizes;

    public SwypeIdSettingsPage(List<Size> sizes) {
        this.sizes = sizes.toArray(new Size[sizes.size()]);
    }

    private SwypeIdSettingsPage(Parcel in) {
        sizes = in.createTypedArray(Size.CREATOR);
    }

    @Override
    public Fragment makeFragment() {
        return SwypeIdSettingsFragment.instantiate(this);
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
        dest.writeTypedArray(sizes, flags);
    }
}
