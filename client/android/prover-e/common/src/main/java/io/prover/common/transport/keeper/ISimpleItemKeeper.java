package io.prover.common.transport.keeper;

import android.support.annotation.Nullable;

public interface ISimpleItemKeeper<T> {

    void requestValue(@Nullable final ISimpleGetObjectCallback<T> listener, boolean forceUpdate);

    T getValue();

    boolean isOutdated();

    void addListener(ISimpleGetObjectCallback<T> listener);

    void removeListener(ISimpleGetObjectCallback<T> listener);

    void clear();
}
