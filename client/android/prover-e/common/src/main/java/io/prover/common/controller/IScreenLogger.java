package io.prover.common.controller;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import io.prover.common.view.ScreenLogger;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public interface IScreenLogger {

    void addText(CharSequence text, @MessageType int type);

    @IntDef({
            ScreenLogger.MessageType.GENERAL,
            ScreenLogger.MessageType.NETWORK_ERROR,
            ScreenLogger.MessageType.ERROR,
            ScreenLogger.MessageType.DETECTOR,
            ScreenLogger.MessageType.NETWORK
    })
    @Retention(SOURCE)
    @interface MessageType {
        /**
         * undefined message type
         */
        int GENERAL = 0;
        /**
         * Network error
         */
        int NETWORK_ERROR = 1;
        /**
         * Error
         */
        int ERROR = 2;
        /**
         * detection details from detector
         */
        int DETECTOR = 3;
        /**
         * network requests
         */
        int NETWORK = 4;
    }
}
