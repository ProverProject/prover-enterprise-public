package io.prover.common.enterprise.auth;

import android.os.Parcel;
import android.os.Parcelable;

public class AuthActivityPreferences implements Parcelable {
    public static final Creator<AuthActivityPreferences> CREATOR = new Creator<AuthActivityPreferences>() {
        @Override
        public AuthActivityPreferences createFromParcel(Parcel in) {
            return new AuthActivityPreferences(in);
        }

        @Override
        public AuthActivityPreferences[] newArray(int size) {
            return new AuthActivityPreferences[size];
        }
    };

    public final Class mainActivityClass;
    public final int subtitleDrawableId;
    public final int helpImagesId;
    public final int helpStringsId;


    public AuthActivityPreferences(Class mainActivityClass, int subtitleDrawableId, int helpImagesId, int helpStringsId) {
        this.mainActivityClass = mainActivityClass;
        this.subtitleDrawableId = subtitleDrawableId;
        this.helpImagesId = helpImagesId;
        this.helpStringsId = helpStringsId;
    }

    private AuthActivityPreferences(Parcel in) {
        subtitleDrawableId = in.readInt();
        helpImagesId = in.readInt();
        helpStringsId = in.readInt();
        String className = in.readString();
        try {
            mainActivityClass = Class.forName(className, false, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public HelpPage helpPage() {
        return new HelpPage(subtitleDrawableId, helpImagesId, helpStringsId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(subtitleDrawableId);
        dest.writeInt(helpImagesId);
        dest.writeInt(helpStringsId);
        dest.writeString(mainActivityClass.getName());
    }
}
