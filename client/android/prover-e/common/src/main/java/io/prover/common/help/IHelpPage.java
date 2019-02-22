package io.prover.common.help;


import io.prover.common.enterprise.auth.IAuthPage;

public interface IHelpPage<E extends Enum<E>> extends IAuthPage<E> {
    int getHelpImageIds();

    int getHelpStringIds();
}
