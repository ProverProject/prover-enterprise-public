package io.prover.clapperboard.enterprise;

import io.prover.common.enterprise.auth.AuthActivityPreferences;

public class SplashActivity extends io.prover.common.enterprise.auth.SplashActivity {

    @Override
    protected AuthActivityPreferences getPreferences() {
        return Settings.authActivityPreferences();
    }
}