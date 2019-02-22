package io.prover.common.enterprise.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import io.prover.common.R;
import io.prover.common.help.HelpFragment;
import io.prover.common.help.IHelpPage;
import io.prover.common.pages.base.PageBase;

public class AuthPage extends PageBase<AuthPageType> implements IAuthPage<AuthPageType> {

    public static final Creator<AuthPage> CREATOR = new Creator<AuthPage>() {
        @Override
        public AuthPage createFromParcel(Parcel in) {
            return new AuthPage(in);
        }

        @Override
        public AuthPage[] newArray(int size) {
            return new AuthPage[size];
        }
    };

    public final int subtitleDrawableId;

    public AuthPage(@NonNull AuthPageType type, int subtitleDrawableId) {
        super(type);
        this.subtitleDrawableId = subtitleDrawableId;
    }

    public AuthPage(@NonNull AuthPageType type, @NonNull AuthPage oldPage) {
        super(type);
        this.subtitleDrawableId = oldPage.subtitleDrawableId;
    }

    protected AuthPage(Parcel in) {
        super(in, AuthPageType.class);
        subtitleDrawableId = in.readInt();
    }

    @Override
    public int getTitleDrawableId() {
        return R.drawable.ic_prover_enterprise;
    }

    @Override
    public Intent intent(Context context) {
        switch (type) {
            case Splash:
            case Login:
            case Help:
                if (context instanceof AuthActivity) {
                    Intent intent = new Intent(context, AuthActivity.class);
                    intent.putExtra(PAGE_ARG_NAME, this);
                    return intent;
                }
                return null;

            default:
                return null;
        }
    }

    public Intent intent(Context context, Class activityClass) {
        switch (type) {
            case Splash:
            case Login:
            case Help:
                Intent intent = new Intent(context, activityClass);
                intent.putExtra(PAGE_ARG_NAME, this);
                return intent;

            default:
                return null;
        }
    }

    @Override
    public boolean isHomeAsUp() {
        return false;
    }

    @Override
    public Fragment makeFragment() {
        switch (type) {
            case Splash:
                return SplashFragment.newInstance(this);

            case Login:
                return EnterpriseLoginFragment.newInstance(this);

            case Help:
                return HelpFragment.newInstance((IHelpPage) this);

            default:
                throw new RuntimeException("Not implemented for: " + type.name());
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(subtitleDrawableId);
    }

    @Override
    public int getSubtitleDrawableId() {
        return subtitleDrawableId;
    }
}
