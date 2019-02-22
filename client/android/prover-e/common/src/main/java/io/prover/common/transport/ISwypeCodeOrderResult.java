package io.prover.common.transport;

public interface ISwypeCodeOrderResult {
    String getSwypeCode();

    OrderType getOrderType();

    boolean isFake();
}
