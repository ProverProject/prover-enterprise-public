package io.prover.common.enterprise.auth;

import io.prover.common.pages.base.IPage;

public interface IAuthPage<E extends Enum<E>> extends IPage<E> {
    int getTitleDrawableId();

    int getSubtitleDrawableId();
}
