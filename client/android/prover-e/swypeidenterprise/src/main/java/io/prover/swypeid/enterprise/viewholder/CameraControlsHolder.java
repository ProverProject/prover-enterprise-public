package io.prover.swypeid.enterprise.viewholder;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Locale;

import io.prover.common.enterprise.preferences.LicensePage;
import io.prover.common.enterprise.view.BalanceHolderEnterprise;
import io.prover.common.enterprise.view.BalanceStatusHolder;
import io.prover.common.enterprise.view.FeeEstimateViewHolder;
import io.prover.common.prefs.IPreferencesPage;
import io.prover.common.prefs.WalletAndPreferencesActivity;
import io.prover.common.util.UtilFile;
import io.prover.swypeid.camera2.Size;
import io.prover.swypeid.enterprise.R;
import io.prover.swypeid.enterprise.model.SwypeIdRootModel;
import io.prover.swypeid.enterprise.settings.SwypeIdSettingsPage;
import io.prover.swypeid.model.CameraRecordConfig;
import io.prover.swypeid.model.SwypeIdMission;
import io.prover.swypeid.model.VideoRecorder;
import io.prover.swypeid.viewholder.ISwypeViewHolder;
import io.prover.swypeid.viewholder.SwypeViewHolderV2;

import static io.prover.swypeid.enterprise.Const.KEY_SELECTED_RESOLUTION_X;
import static io.prover.swypeid.enterprise.Const.KEY_SELECTED_RESOLUTION_Y;
import static io.prover.swypeid.enterprise.Const.KEY_SHOW_FPS;

/**
 * Created by babay on 07.11.2017.
 */

public class CameraControlsHolder implements View.OnClickListener,
        VideoRecorder.OnPreviewStartListener,
        VideoRecorder.OnRecordingStartListener, VideoRecorder.OnRecordingStopListener, SwypeIdMission.OnFpsUpdateListener, View.OnLongClickListener {
    private final ViewGroup root;
    private final Activity activity;
    private final SwypeIdMission mission;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final TextView fpsView;
    private final ImageButton recordButton;
    private final ISwypeViewHolder swypeViewHolder;
    private final FeeEstimateViewHolder offerHolder;
    private final View settingsButton;
    private final BalanceStatusHolder balanceHolder;
    private boolean started;

    public CameraControlsHolder(Activity activity, ConstraintLayout root, SwypeIdRootModel rootModel) {
        this.root = root;
        this.activity = activity;
        this.mission = rootModel.mission;

        fpsView = root.findViewById(R.id.fpsCounter);
        recordButton = root.findViewById(R.id.recordButton);
        settingsButton = activity.findViewById(R.id.settingsBtn);

        recordButton.setOnClickListener(this);
        recordButton.setOnLongClickListener(this);

        fpsView.bringToFront();
        mission.video.previewStart.add(this);
        mission.video.onRecordingStart.add(this);
        mission.video.onRecordingStop.add(this);
        mission.fpsUpdateListener.add(this);
        rootModel.onActivityResume.add(this::onActivityResume);

        swypeViewHolder = SwypeViewHolderV2.inflate(root, mission);
        balanceHolder = new BalanceStatusHolder(root, rootModel.transport);
        offerHolder = new FeeEstimateViewHolder(activity.findViewById(R.id.offer), rootModel.transport);

        offerHolder.setShowWalletRunnable(() -> startWallerActivity(0));
        settingsButton.setOnClickListener((v) -> startWallerActivity(1));
        balanceHolder.setOnOpenWalletClickLictener(v -> startWallerActivity(0));
    }

    private void startWallerActivity(int page) {
        if (!mission.video.isRecording()) {
            IPreferencesPage[] pages = new IPreferencesPage[]{new LicensePage(), new SwypeIdSettingsPage(mission.video.getAvailableVideoSizes())};
            WalletAndPreferencesActivity.start(activity, page, pages, BalanceHolderEnterprise.class);
        }
    }

    @Override
    @UiThread
    public void onClick(View v) {
        if (v == recordButton) {
            if (mission.video.isRecording()) {
                mission.requestFinishRecording(false);
            } else {
                mission.startRecording();
            }
        }
    }

    private void updateControls(boolean wasPlaying, boolean playing) {
        if (wasPlaying == playing)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimatedVectorDrawable adr;
            if (playing) {
                adr = (AnimatedVectorDrawable) root.getContext().getDrawable(R.drawable.ic_record_start_animated);
            } else {
                adr = (AnimatedVectorDrawable) root.getContext().getDrawable(R.drawable.ic_record_stop_animated);
            }
            adr.setBounds(0, 0, adr.getIntrinsicWidth(), adr.getIntrinsicHeight());
            recordButton.setImageDrawable(adr);
            adr.start();
        } else {
            Drawable dr = AppCompatResources.getDrawable(root.getContext(),
                    playing ? R.drawable.ic_stop_record_icon : R.drawable.ic_start_record_icon);
            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
            recordButton.setImageDrawable(dr);
        }
    }

    public void onStart() {
        started = true;
        updateControls(true, false);
    }

    public void onStop() {
        started = false;
    }

    @Override
    public void onPreviewStart(@NonNull List<Size> sizes, @NonNull Size previewSize) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(root.getContext());
        Size resolution = Size.fromPreferences(prefs, KEY_SELECTED_RESOLUTION_X, KEY_SELECTED_RESOLUTION_Y);
        if (!previewSize.equalsIgnoringRotation(resolution)) {
            SharedPreferences.Editor editor = prefs.edit();
            previewSize.saveToPreferences(editor, KEY_SELECTED_RESOLUTION_X, KEY_SELECTED_RESOLUTION_Y);
            editor.apply();
        }
    }

    @Override
    public void onRecordingStart(CameraRecordConfig config) {
        offerHolder.setVisible(false);
        settingsButton.setVisibility(View.GONE);
        updateControls(false, true);
    }

    @Override
    @MainThread
    public void onRecordingStop(File file, boolean isVideoConfirmed) {
        if (file != null) {
            Snackbar.make(root, "Finished file: " + file.getPath(), Snackbar.LENGTH_LONG)
                    .setAction("Open", v1 -> new UtilFile(file).externalOpenFile(root.getContext(), null))
                    .show();
        }
        updateControls(true, false);
        swypeViewHolder.hide();
        offerHolder.setVisible(true);
        settingsButton.setVisibility(View.VISIBLE);
    }


    @Override
    public void OnFpsUpdate(float fps, float processorFps) {
        fpsView.setText(String.format(Locale.getDefault(), "%.1f/%.1f fps ", fps, processorFps));
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.recordButton:
                swypeViewHolder.toggleDefectView();
                return true;
        }
        return false;
    }

    private void onActivityResume(Activity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(root.getContext());
        boolean showFps = prefs.getBoolean(KEY_SHOW_FPS, false);
        fpsView.setVisibility(showFps ? View.VISIBLE : View.GONE);

        Size resolution = Size.fromPreferences(prefs, KEY_SELECTED_RESOLUTION_X, KEY_SELECTED_RESOLUTION_Y);
        if (resolution != null)
            mission.camera.setCameraResolution(resolution);
        offerHolder.setVisible(true);
        settingsButton.setVisibility(View.VISIBLE);
    }
}