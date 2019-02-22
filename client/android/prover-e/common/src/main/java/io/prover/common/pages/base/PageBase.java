package io.prover.common.pages.base;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * Created by babay on 17.08.2017.
 */

public abstract class PageBase<E extends Enum<E>> implements Parcelable, IPage<E> {

    public static final String PAGE_ARG_NAME = "page";

    /*public static final Creator<PageBase> CREATOR = new Creator<PageBase>() {
        @Override
        public PageBase createFromParcel(Parcel in) {
            return new PageBase(in);
        }

        @Override
        public PageBase[] newArray(int size) {
            return new PageBase[size];
        }
    };*/
    @NonNull
    public final E type;

    public PageBase(@NonNull E type) {
        this.type = type;
    }

    protected PageBase(Parcel in, Class<E> eClass) {
        type = eClass.getEnumConstants()[in.readByte()];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) type.ordinal());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageBase)) return false;

        PageBase<?> pageBase = (PageBase<?>) o;

        return type == pageBase.type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    public abstract boolean isHomeAsUp();

    public abstract Fragment makeFragment();

    @Override
    public String toString() {
        return "PageBase{" +
                "type=" + type +
                '}';
    }

    @NonNull
    @Override
    public E getType() {
        return type;
    }
}
