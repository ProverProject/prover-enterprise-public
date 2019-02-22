package io.prover.common.transport.keeper;

public interface ISimpleGetObjectCallback<T> {
    void onRequestResult(T responce);
}
