package io.prover.swypeid.enterprise.viewholder;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;

import java.io.File;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.Queue;

import io.prover.common.enterprise.transport.response.EnterpriseBalance;
import io.prover.common.transport.OrderType;
import io.prover.swypeid.detector.DetectionState;
import io.prover.swypeid.detector.DetectionStateChange;
import io.prover.swypeid.enterprise.model.SwypeIdRootModel;
import io.prover.swypeid.enterprise.viewholder.model.Hint;
import io.prover.swypeid.enterprise.viewholder.model.Hints;
import io.prover.swypeid.model.SwypeCode;
import io.prover.swypeid.model.SwypeIdDetector;
import io.prover.swypeid.model.VideoRecorder;

import static io.prover.swypeid.enterprise.viewholder.IHintHolder.HintState;

public class HintsViewHolder implements IHintHolder.HintListener, VideoRecorder.OnRecordingStopListener,
        SwypeIdDetector.OnSwypeCodeSetListener, SwypeIdDetector.OnDetectionStateCahngedListener {
    private final ConstraintLayout root;

    private final Queue<Hint> postHintQueue = new LinkedList<>();
    private HintTimedHolder currentHintHolder;
    private HintSteadyHolder steadyHintHolder;
    private boolean canShowHints = true;
    private HintSteadyHolder lowContrastHint;

    public HintsViewHolder(ConstraintLayout root, SwypeIdRootModel controller) {
        this.root = root;

        controller.mission.video.onRecordingStop.add(this);
        controller.mission.detector.swypeCodeSet.add(this);
        controller.mission.detector.detectionState.add(this);
        controller.mission.postprocessingListener.add(this::onPostprocessingStartedListener);

        controller.transport.onBalanceUpdateListener.add(this::onBalanceUpdated);
        controller.mission.videoPostedToBlockchainListener.add(this::onVideoPostedToBlockchain);
        controller.transport.onOrderRequestFailed.add(this::onOrderRequestError);
        controller.transport.onPostFileOrderComplete.add((result, timeSpent) -> showHint(Hints.FileHashAddedToBlockchain));
    }

    private void showHint(Hint hint) {
        if (!canShowHints && !hint.forcedHint)
            return;

        if (hint.hint == Hints.LowContrast) {
            if (lowContrastHint != null)
                lowContrastHint.cancel();
            lowContrastHint = new HintSteadyHolder(root, hint, this);
            lowContrastHint.show();
            return;
        }

        if (canShowNextHint()) {
            if (hint.isTimedHint()) {
                currentHintHolder = new HintTimedHolder(root, hint, this);
                if (postHintQueue.size() > 0)
                    currentHintHolder.setShowShort();
                currentHintHolder.show();
            } else {
                if (steadyHintHolder == null) {
                    steadyHintHolder = new HintSteadyHolder(root, hint, this);
                    steadyHintHolder.show();
                } else {
                    steadyHintHolder.hide();
                    postHintQueue.add(hint);
                }
            }
        } else {
            postHintQueue.add(hint);
        }
    }

    private void showHint(Hints hint) {
        showHint(hint.object());
    }

    @Override
    public void onHintHidden(IHintHolder hintHolder) {
        if (hintHolder == currentHintHolder)
            currentHintHolder = null;
        else if (hintHolder == steadyHintHolder)
            steadyHintHolder = null;
        else if (hintHolder == lowContrastHint)
            lowContrastHint = null;

        if (postHintQueue.size() > 0 && canShowNextHint()) {
            Hint nextHint = postHintQueue.poll();
            if (nextHint != null)
                showHint(nextHint);
        }
    }

    @Override
    public void OnHintMinified(IHintHolder hintHolder) {
        if (hintHolder == steadyHintHolder && canShowNextHint()) {
            Hint nextHint = postHintQueue.poll();
            if (nextHint != null)
                showHint(nextHint);
        }
    }

    private boolean canShowNextHint() {
        if (currentHintHolder != null) {
            currentHintHolder.hide();
            return false;
        }

        IHintHolder.HintState steadyState = steadyHintHolder == null ?
                HintState.Hidden : steadyHintHolder.getState();

        return steadyState != HintState.Visible && steadyState != HintState.Minifying && steadyState != HintState.ExpectingToShow;
    }

    @Override
    public void onRecordingStop(File file, boolean isVideoConfirmed) {
        canShowHints = true;
        if (steadyHintHolder != null) {
            steadyHintHolder.hide();
        }
        if (file != null && !isVideoConfirmed) {
            if (steadyHintHolder == null)
                showHint(Hints.VideoNotConfirmed);
            else
                postHintQueue.add(Hints.VideoNotConfirmed.object());
        }
        if (steadyHintHolder != null) {
            steadyHintHolder.hide();
            steadyHintHolder = null;
        }
    }

    @Override
    public void onSwypeCodeSet(SwypeCode swypeCode, SwypeCode actualSwypeCode, Integer orientationHint) {
        showHint(Hints.MakeCircular);
    }

    @Override
    public void onDetectionStateChanged(@NonNull DetectionStateChange stateChange) {
        switch (stateChange.event) {
            case FailedSwypeCode:
                canShowHints = true;
                showHint(Hints.SwypeCodeFailed);
                break;

            case CompletedSwypeCode:
                canShowHints = true;
                showHint(Hints.AllDone);
                break;

            case CircleDetected:
                canShowHints = false;
                postHintQueue.clear();
                if (currentHintHolder != null)
                    currentHintHolder.hide();
                if (steadyHintHolder != null) {
                    steadyHintHolder.hide();
                }
                break;
        }

        if (stateChange.state.message == DetectionState.Message.LOW_CONTRAST) {
            if (lowContrastHint == null || lowContrastHint.isHiding())
                showHint(Hints.LowContrast.object());
        } else if (lowContrastHint != null) {
            lowContrastHint.hide();
        }
    }

    private void onPostprocessingStartedListener() {
        showHint(Hints.CalculatingVideoHash);
    }

    private void onBalanceUpdated(EnterpriseBalance balance) {
        if (balance.proof.amount.equals(BigInteger.ZERO) || balance.xem.amount.equals(BigInteger.ZERO)) {
            showHint(Hints.NoMoney);
        }
    }

    private void onVideoPostedToBlockchain(boolean confirmed) {
        showHint(Hints.VideoHashPosted);
    }

    private void onOrderRequestError(@NonNull OrderType orderType, Exception e) {
        if (orderType == OrderType.FileHash) {
            showHint(Hints.ErrorPostingHash);
        }
    }
}