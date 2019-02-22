package io.prover.common.enterprise.auth;

import android.os.Parcel;
import android.support.annotation.NonNull;

import io.prover.common.R;
import io.prover.common.help.IHelpPage;

public class HelpPage extends AuthPage implements IHelpPage<AuthPageType> {

    public static final Creator<HelpPage> CREATOR = new Creator<HelpPage>() {
        @Override
        public HelpPage createFromParcel(Parcel in) {
            return new HelpPage(in);
        }

        @Override
        public HelpPage[] newArray(int size) {
            return new HelpPage[size];
        }
    };

    public final int helpImageIds;
    public final int helpStringIds;

    public HelpPage(@NonNull AuthPage oldPage, int helpImageIds, int helpStringIds) {
        super(AuthPageType.Help, oldPage);
        this.helpImageIds = helpImageIds;
        this.helpStringIds = helpStringIds;
    }

    public HelpPage(int subtitleDrawableId, int helpImageIds, int helpStringIds) {
        super(AuthPageType.Help, subtitleDrawableId);
        this.helpImageIds = helpImageIds;
        this.helpStringIds = helpStringIds;
    }

    private HelpPage(Parcel in) {
        super(in);
        helpImageIds = in.readInt();
        helpStringIds = in.readInt();
    }

    public static HelpPage helpSwypeId(int subtitleDrawableId) {
        return new HelpPage(subtitleDrawableId, R.array.swypeid_help_images, R.array.swypeid_help_strings);
    }

    public static HelpPage helpClapperboard(int subtitleDrawableId) {
        return new HelpPage(subtitleDrawableId, R.array.clapperboard_help_images, R.array.clapperboard_help_strings);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(helpImageIds);
        dest.writeInt(helpStringIds);
    }

    @Override
    public int getHelpImageIds() {
        return helpImageIds;
    }

    @Override
    public int getHelpStringIds() {
        return helpStringIds;
    }
}
