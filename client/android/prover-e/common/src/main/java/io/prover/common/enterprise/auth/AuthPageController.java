package io.prover.common.enterprise.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import io.prover.common.pages.base.PageController;

import static io.prover.common.Const.ARG_TUTORIAL_SHOWN;
import static io.prover.common.enterprise.auth.AuthActivity.ARG_START_ACTIVITY;

public class AuthPageController extends PageController<AuthPageType, AuthPage> {

    private final int subtitleDrawableId;

    AuthPageController(AppCompatActivity activity, int fragmentContainerId, int subtitleDrawableId) {
        super(activity, fragmentContainerId);
        this.subtitleDrawableId = subtitleDrawableId;
    }

    @Override
    @NonNull
    public AuthPage getDefaultPage() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if (!prefs.contains(ARG_TUTORIAL_SHOWN)) {
            return new AuthPage(AuthPageType.Help, subtitleDrawableId);
        }

        if (activity instanceof SplashActivity)
            return new AuthPage(AuthPageType.Login, subtitleDrawableId);

        Intent intent = activity.getIntent();
        if (intent != null && intent.getBooleanExtra(ARG_START_ACTIVITY, false)) {
            return new AuthPage(AuthPageType.Login, subtitleDrawableId);
        } else {
            return new AuthPage(AuthPageType.ShowMainPage, subtitleDrawableId);
        }
    }

    @Override
    protected boolean shouldAddToBackstack(@Nullable AuthPage oldPage, @NonNull AuthPage page) {
        if (oldPage == null)
            return false;
        switch (page.type) {
            case Help:
                return true;

            case Login:
                switch (oldPage.type) {
                    case Login:
                    case Help:
                        return true;
                }
                return false;
        }
        return false;
    }

    @Override
    protected boolean shouldClearBackstack(@Nullable AuthPage oldPage, @NonNull AuthPage page) {
        switch (page.type) {
            case Login:
                if (oldPage == null)
                    return false;
                switch (oldPage.type) {
                    case Login:
                        return true;
                }
                return false;
        }
        return false;
    }
}
