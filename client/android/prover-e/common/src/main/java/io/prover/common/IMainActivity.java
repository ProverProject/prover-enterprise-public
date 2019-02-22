package io.prover.common;

import io.prover.common.controller.RootModel;

public interface IMainActivity<T extends RootModel> {
    T getController();
}
