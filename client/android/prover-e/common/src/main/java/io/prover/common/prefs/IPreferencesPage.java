package io.prover.common.prefs;

import android.content.res.Resources;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

public interface IPreferencesPage extends Parcelable {
    Fragment makeFragment();

    String getPageTitle(Resources resources);
}
