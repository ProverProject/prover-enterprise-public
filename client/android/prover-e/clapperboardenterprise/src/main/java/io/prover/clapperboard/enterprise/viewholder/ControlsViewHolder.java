package io.prover.clapperboard.enterprise.viewholder;

import android.support.annotation.UiThread;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import io.prover.clapperboard.ClapperboardMissionModel;
import io.prover.clapperboard.ClapperboardMissionModel.ClapperboardMissionState;
import io.prover.clapperboard.enterprise.MainActivity;
import io.prover.clapperboard.enterprise.R;
import io.prover.clapperboard.enterprise.Settings;
import io.prover.clapperboard.enterprise.model.ClapperboardRootModel;
import io.prover.clapperboard.enterprise.settings.ClapperboardSettingsPage;
import io.prover.clapperboard.view.QrCodeViewHolder;
import io.prover.common.controller.MissionModel;
import io.prover.common.enterprise.auth.AuthPageType;
import io.prover.common.enterprise.preferences.LicensePage;
import io.prover.common.enterprise.transport.EnterpriseTransportModel;
import io.prover.common.enterprise.view.BalanceHolderEnterprise;
import io.prover.common.enterprise.view.BalanceStatusHolder;
import io.prover.common.enterprise.view.FeeEstimateViewHolder;
import io.prover.common.prefs.IPreferencesPage;
import io.prover.common.prefs.WalletAndPreferencesActivity;
import io.prover.common.util.TextInputLayoutErrorWatcher;

import static io.prover.clapperboard.ClapperboardMissionModel.ClapperboardMissionState.HAVE_QR_CODE;
import static io.prover.clapperboard.ClapperboardMissionModel.ClapperboardMissionState.READY;
import static io.prover.clapperboard.ClapperboardMissionModel.ClapperboardMissionState.REQUESTING_QR_CODE;

/**
 * Created by babay on 21.12.2017.
 */

public class ControlsViewHolder implements View.OnClickListener, MissionModel.ControllerStateChangedListener {

    private final ClapperboardMissionModel missionModel;
    private final ViewGroup contentRoot;
    private final TextInputLayout textInputLayout;
    private final TextView largeMessageView;
    private final FabHolder fabHolder;
    private final MainActivity activity;
    private final QrCodeViewHolder qrHolder;
    private final FeeEstimateViewHolder offerHolder;
    private final EnterpriseTransportModel transport;

    private int textBlocksInFeeEstimate = 0;

    //private Mode mode = Mode.Initial;

    public ControlsViewHolder(MainActivity activity, ClapperboardRootModel controller) {
        this.missionModel = controller.mission;
        this.transport = controller.transport;
        this.activity = activity;

        contentRoot = activity.findViewById(R.id.contentRoot);
        textInputLayout = activity.findViewById(R.id.textInput);
        largeMessageView = activity.findViewById(R.id.largeMessageView);

        qrHolder = new QrCodeViewHolder(activity.findViewById(R.id.qrCodeContainer));
        offerHolder = new FeeEstimateViewHolder(contentRoot.findViewById(R.id.offer), controller.transport);

        new BalanceStatusHolder(activity.findViewById(R.id.balanceContainer), controller.transport)
                .setOnOpenWalletClickLictener((v) -> {
                    if (missionModel.getState() == ClapperboardMissionState.READY) {
                        startPreferencesActivity(0);
                    }
                });

        activity.findViewById(R.id.helpLink).setOnClickListener(v
                -> activity.startActivity(Settings.authActivityIntent(activity, AuthPageType.Help)));

        activity.findViewById(R.id.settingsBtn).setOnClickListener(v -> {
            if (missionModel.getState() == ClapperboardMissionState.READY) {
                startPreferencesActivity(1);
            }
        });

        offerHolder.setShowWalletRunnable(() -> {
            if (missionModel.getState() == ClapperboardMissionState.READY)
                startPreferencesActivity(0);
        });

        controller.mission.onControllerStateListener.add(this);

        fabHolder = new FabHolder(activity.findViewById(R.id.fab));
        fabHolder.setOnClickListener(this);

        new TextInputLayoutErrorWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                int len = textInputLayout.getEditText().getText().toString().getBytes().length;

                int blocks = (len + 6) / 32;
                if (blocks != textBlocksInFeeEstimate) {
                    textBlocksInFeeEstimate = blocks;
                    transport.estimateQrCodeFee(textInputLayout.getEditText().getText().toString());
                }
            }
        }
                .addTextInputLayout(textInputLayout);
    }

    private void startPreferencesActivity(int page) {
        IPreferencesPage[] pages = new IPreferencesPage[]{new LicensePage(), new ClapperboardSettingsPage()};
        WalletAndPreferencesActivity.start(activity, page, pages, BalanceHolderEnterprise.class);
    }

    @Override
    @UiThread
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                switch (missionModel.getState()) {
                    case READY:
                        if (missionModel.canStartMainTask()) {
                            String message = textInputLayout.getEditText().getText().toString();
                            missionModel.requestQrCode(message);
                        } else {
                            Toast.makeText(activity, R.string.cantGetPriceToast, Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case REQUESTING_QR_CODE:
                    case HAVE_QR_CODE:
                        missionModel.onFinishedMainTask();
                        missionModel.clearStoredState(activity);
                        break;
                }
                break;
        }
    }

    @Override
    public void onControllerStateChanged(@ClapperboardMissionState int oldState, @ClapperboardMissionState int newState) {
        switch (newState) {
            case READY:
                textInputLayout.setEnabled(true);
                largeMessageView.setVisibility(View.GONE);
                textInputLayout.setVisibility(View.VISIBLE);
                fabHolder.setState(false, oldState != ClapperboardMissionState.NOT_READY);
                contentRoot.setKeepScreenOn(false);
                offerHolder.setVisible(true);
                qrHolder.hide();
                break;

            case REQUESTING_QR_CODE:
                largeMessageView.setText(R.string.requestingQrCode);
                TransitionManager.beginDelayedTransition(contentRoot);
                textInputLayout.setEnabled(false);
                largeMessageView.setVisibility(View.VISIBLE);
                fabHolder.setState(true, true);
                contentRoot.setKeepScreenOn(false);
                offerHolder.setVisible(false);
                break;

            case HAVE_QR_CODE:
                qrHolder.setCode(missionModel.getQrCodeOrder());
                contentRoot.setKeepScreenOn(true);
                fabHolder.setState(true, false);
                textInputLayout.getEditText().setText("");
                qrHolder.show();
                break;
        }
    }
}
