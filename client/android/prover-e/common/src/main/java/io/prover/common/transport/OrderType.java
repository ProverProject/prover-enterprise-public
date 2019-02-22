package io.prover.common.transport;

public enum OrderType {
    /**
     * order to create QR-code
     */
    QrCode,

    /**
     * Create Swype-Full (with posting a transaction to a blockChain)
     */
    SwypeFull,

    /**
     * post file hash
     */
    FileHash,

    /**
     * create swype code, server decides, fast or full
     */
    SwypeServerDefault,

    /**
     * create swype code fast
     */
    SwypeFast,

    /**
     * post file hash without swype code
     */
    HashNoSwype
}
