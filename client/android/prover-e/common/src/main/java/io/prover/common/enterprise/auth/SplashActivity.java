package io.prover.common.enterprise.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.prover.common.R;

public abstract class SplashActivity extends AppCompatActivity implements AuthFragmentInteractionListener {

    final AuthPageController pageController;

    private final AuthActivityPreferences preferences;

    public SplashActivity() {
        preferences = getPreferences();
        pageController = new AuthPageController(this, R.id.content, preferences.subtitleDrawableId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        showPage(new AuthPage(AuthPageType.Splash, preferences.subtitleDrawableId));
    }

    @Override
    public void showPage(AuthPage page) {
        if (page == null) {
            showPage(pageController.getDefaultPage());
            return;
        }

        switch (page.type) {
            case ShowMainPage:
                finish();
                Intent intent = new Intent(this, preferences.mainActivityClass);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            case Splash:
                pageController.showPage(page);
                break;

            default:
                finish();
                intent = new Intent(this, AuthActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(AuthActivity.ARG_PREFS, preferences);
                intent.putExtra(AuthActivity.ARG_START_ACTIVITY, true);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void closeFragment() {
    }

    protected abstract AuthActivityPreferences getPreferences();
}