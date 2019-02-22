package io.prover.swypeid.enterprise;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.prover.common.enterprise.auth.AuthActivity;
import io.prover.common.enterprise.auth.AuthActivityPreferences;
import io.prover.common.enterprise.auth.AuthPage;
import io.prover.common.enterprise.auth.AuthPageType;
import io.prover.common.pages.base.PageBase;

/**
 * Created by babay on 15.11.2017.
 */

public interface Settings {
    static Intent authActivityIntent(@NonNull Context context, @Nullable AuthPageType pageType) {
        Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra(AuthActivity.ARG_PREFS, authActivityPreferences());
        if (pageType != null)
            intent.putExtra(PageBase.PAGE_ARG_NAME, new AuthPage(pageType, R.drawable.ic_swype_id));
        return intent;
    }

    static AuthActivityPreferences authActivityPreferences() {
        return new AuthActivityPreferences(MainActivity.class, R.drawable.ic_swype_id,
                io.prover.common.R.array.swypeid_help_images_enterprise, io.prover.common.R.array.swypeid_help_strings_enterprise);
    }
}