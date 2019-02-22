package io.prover.common.controller;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.util.Locale;

import io.prover.common.transport.OrderType;
import io.prover.common.transport.TransportModel;
import io.prover.common.transport.base.FormRequest;
import io.prover.common.transport.base.NetworkRequest;
import io.prover.common.view.ScreenLogger;

public class Logger {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private IScreenLogger logger;

    public Logger(TransportModel transportModel) {
        transportModel.onNetworkRequestStartListener.add(this::onNetworkRequestStart);
        transportModel.onNetworkRequestDoneListener.add(this::onNetworkRequestDone);
        transportModel.onNetworkRequestCancelListener.add(this::onNetworkRequestCancel);
        transportModel.onNetworkRequestErrorListener.add(this::onNetworkRequestError);
        transportModel.onQrCodeOrderComplete.add((result, timeSpent) -> onOrderComplete(OrderType.QrCode, timeSpent));
        transportModel.onSwypeCodeOrderComplete.add((r, timeSpent) ->
                onOrderComplete(r == null ? null : r.getOrderType(), timeSpent));
        transportModel.onPostFileOrderComplete.add((r, timeSpent) ->
                onOrderComplete(r == null ? null : r.getOrderType(), timeSpent));
    }

    public void setModelRoot(RootModel controller) {
        controller.mission.generalErrorListener.add(this::onControllerException);
    }

    public void setView(IScreenLogger logger) {
        this.logger = logger;
    }

    public void addToScreenLog(CharSequence text, @IScreenLogger.MessageType int type) {
        if (logger != null) {
            if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
                logger.addText(text, type);
            } else {
                handler.post(() -> {
                    if (logger != null) logger.addText(text, type);
                });
            }
        }
    }

    protected void onNetworkRequestStart(NetworkRequest request) {
        if (request instanceof FormRequest)
            addToScreenLog("Starting request: " + ((FormRequest) request).getMethod(), ScreenLogger.MessageType.NETWORK);

    }

    protected void onNetworkRequestDone(NetworkRequest request, Object responce) {
        if (request instanceof FormRequest) {
            addToScreenLog("Request done: " + ((FormRequest) request).getMethod(), ScreenLogger.MessageType.NETWORK);
        }
    }

    protected void onNetworkRequestCancel(NetworkRequest request) {
        if (request instanceof FormRequest)
            addToScreenLog("Request cancelled: " + ((FormRequest) request).getMethod(), ScreenLogger.MessageType.NETWORK);
    }

    protected void onNetworkRequestError(NetworkRequest request, Exception e) {
        if (request instanceof FormRequest) {
            String method = ((FormRequest) request).getMethod();
            addToScreenLog("Network error: " + method, IScreenLogger.MessageType.NETWORK_ERROR);
        } else {
            String message = e.getMessage();
            if (message == null || message.length() == 0)
                message = e.getClass().getName();
            addToScreenLog(message, IScreenLogger.MessageType.ERROR);
        }
    }

    private void onControllerException(Exception e, NetworkRequest networkRequest) {

    }

    private void onOrderComplete(@Nullable OrderType orderSubtype, long timeSpent) {
        if (orderSubtype == null) {
            addToScreenLog("Order cancelled", IScreenLogger.MessageType.GENERAL);
        } else {
            String message = String.format(Locale.getDefault(), "Order %s complete, time to process: %d ms", orderSubtype.name(), timeSpent);
            addToScreenLog(message, IScreenLogger.MessageType.GENERAL);
        }
    }
}
