package io.prover.clapperboard.enterprise;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.prover.common.enterprise.auth.AuthActivity;
import io.prover.common.enterprise.auth.AuthActivityPreferences;
import io.prover.common.enterprise.auth.AuthPage;
import io.prover.common.enterprise.auth.AuthPageType;
import io.prover.common.pages.base.PageBase;

public interface Settings {

    static Intent authActivityIntent(@NonNull Context context, @Nullable AuthPageType pageType) {
        Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra(AuthActivity.ARG_PREFS, authActivityPreferences());
        if (pageType != null)
            intent.putExtra(PageBase.PAGE_ARG_NAME, new AuthPage(pageType, R.drawable.ic_clapperboard));
        return intent;
    }

    static AuthActivityPreferences authActivityPreferences() {
        return new AuthActivityPreferences(MainActivity.class, R.drawable.ic_clapperboard,
                io.prover.common.R.array.clapperboard_help_images_enterprise, R.array.clapperboard_help_strings_enterprise);
    }
}
