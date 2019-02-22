package io.prover.common.pages.base;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public interface IPage<E extends Enum<E>> extends Parcelable {
    Intent intent(Context context);

    @NonNull
    E getType();
}
