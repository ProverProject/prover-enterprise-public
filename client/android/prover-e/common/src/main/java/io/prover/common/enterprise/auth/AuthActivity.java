package io.prover.common.enterprise.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import io.prover.common.R;
import io.prover.common.help.IHelpPage;

public class AuthActivity extends AppCompatActivity implements AuthFragmentInteractionListener {

    public static final String ARG_PREFS = "authPrefs";
    public static final String ARG_START_ACTIVITY = "startActivity";

    protected AuthPageController pageController;

    AuthActivityPreferences preferences;

    public AuthActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Intent intent = getIntent();
        preferences = intent.getParcelableExtra(ARG_PREFS);
        pageController = new AuthPageController(this, R.id.content, preferences.subtitleDrawableId);

        if (savedInstanceState == null) {
            showPage(pageController.parseIntent(getIntent()));
        }
    }

    @Override
    public void setIntent(Intent newIntent) {
        if (!newIntent.hasExtra(ARG_PREFS) && preferences != null) {
            newIntent.putExtra(ARG_PREFS, preferences);
        }
        Intent currentIntent = getIntent();
        if (currentIntent != null && currentIntent.getBooleanExtra(ARG_START_ACTIVITY, false))
            newIntent.putExtra(ARG_START_ACTIVITY, true);
        super.setIntent(newIntent);
    }

    @Override
    public void showPage(@Nullable AuthPage page) {
        if (page == null) {
            showPage(pageController.getDefaultPage());
            return;
        }

        if (page.type == AuthPageType.Help && !(page instanceof IHelpPage))
            page = preferences.helpPage();

        switch (page.type) {
            case ShowMainPage:
                finish();
                Intent intent = new Intent(this, preferences.mainActivityClass);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            default:
                pageController.showPage(page);
        }
    }

    @Override
    public void closeFragment() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        AuthPage current = pageController.getCurrentPage();
        int entryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (current.type == AuthPageType.Help) {
            getSupportFragmentManager().popBackStackImmediate();
            if (entryCount == 0) {
                showPage(null);
            }
        } else
            super.onBackPressed();
    }
}
