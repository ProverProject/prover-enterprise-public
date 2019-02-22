package io.prover.common.prefs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.prover.common.R;

import static io.prover.common.Const.TAG;

public class WalletAndPreferencesActivity extends AppCompatActivity {
    public static final String ARG_PAGE = "page";
    public static final String ARG_PAGES = "pages";
    public static final String ARG_WALLET_CLASS = "walletClass";

    private BalanceHolder balanceHolder;

    public static void start(Context context, int page, IPreferencesPage[] pages, Class<? extends BalanceHolder> balanceHolderClass) {
        Intent intent = new Intent(context, WalletAndPreferencesActivity.class)
                .putExtra(ARG_PAGE, page).putExtra(ARG_PAGES, pages)
                .putExtra(ARG_WALLET_CLASS, balanceHolderClass);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        createBalanceViewHolder();

        ViewPager vpPager = findViewById(R.id.vpPager);

        ((TabLayout) findViewById(R.id.tabs)).setupWithViewPager(vpPager);
        findViewById(R.id.backButton).setOnClickListener((v) -> finish());

        vpPager.setAdapter(createAdapter());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(ARG_PAGE))
            vpPager.setCurrentItem(intent.getIntExtra(ARG_PAGE, 0));
    }

    protected WalletAndPreferencesPagerAdapter createAdapter() {
        Parcelable[] pagesparcelable = getIntent().getParcelableArrayExtra(ARG_PAGES);
        IPreferencesPage[] pages = new IPreferencesPage[pagesparcelable.length];
        for (int i = 0; i < pages.length; i++) {
            pages[i] = (IPreferencesPage) pagesparcelable[i];
        }

        return new WalletAndPreferencesPagerAdapter(this, getSupportFragmentManager(), pages);
    }

    private void createBalanceViewHolder() {
        TextView balanceView = findViewById(R.id.balanceView);
        ImageView refreshButton = findViewById(R.id.walletRefreshButton);

        Class<BalanceHolder> clazz = (Class<BalanceHolder>) getIntent().getSerializableExtra(ARG_WALLET_CLASS);
        try {
            Constructor<BalanceHolder> constructor = clazz.getConstructor(TextView.class, ImageView.class);
            balanceHolder = constructor.newInstance(balanceView, refreshButton);
            balanceHolder.refresh();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.e(TAG, "createBalanceViewHolder: " + clazz.getName(), e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (balanceHolder != null)
            balanceHolder.onActivityPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (balanceHolder != null)
            balanceHolder.onActivityResume();
    }

}