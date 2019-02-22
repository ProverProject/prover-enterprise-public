package io.prover.swypeid.enterprise;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.prover.common.IMainActivity;
import io.prover.common.enterprise.auth.AuthPageType;
import io.prover.common.permissions.PermissionManager;
import io.prover.common.view.ScreenLogger;
import io.prover.swypeid.enterprise.model.SwypeIdRootModel;
import io.prover.swypeid.enterprise.viewholder.CameraControlsHolder;
import io.prover.swypeid.enterprise.viewholder.HintsViewHolder;
import io.prover.swypeid.viewholder.CameraViewHolder2;

public class MainActivity extends AppCompatActivity implements IMainActivity<SwypeIdRootModel>, View.OnClickListener, View.OnLongClickListener {

    private final Handler handler = new Handler();
    HintsViewHolder hintsViewHolder;
    private SwypeIdRootModel swypeIdModel;
    private CameraControlsHolder cameraControlsHolder;
    private ScreenLogger loggerView;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        swypeIdModel = SwypeIdRootModel.create(this);
        setContentView(R.layout.activity_main);

        ConstraintLayout contentRoot = findViewById(R.id.contentRoot);

        new CameraViewHolder2(contentRoot.findViewById(R.id.cameraContainer), this, swypeIdModel);
        hintsViewHolder = new HintsViewHolder(contentRoot, swypeIdModel);
        cameraControlsHolder = new CameraControlsHolder(this, contentRoot, swypeIdModel);

        View infoButton = findViewById(R.id.infoButton);
        infoButton.setOnClickListener(this);
        infoButton.setOnLongClickListener(this);

        loggerView = new ScreenLogger(contentRoot, R.id.bottomControlsLayout, io.prover.common.R.id.balanceContainer);
        loggerView.setVisible(false);
        swypeIdModel.mission.logger.setView(loggerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraControlsHolder.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cameraControlsHolder.onStop();
        swypeIdModel.mission.camera.onActivityStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        handler.post(() -> PermissionManager.onPermissionRequestDone(this, requestCode, permissions, grantResults));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.infoButton:
                if (!swypeIdModel.mission.video.isRecording())
                    startActivity(Settings.authActivityIntent(this, AuthPageType.Help));
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.infoButton:
                if (swypeIdModel != null) {
                    loggerView.toggleVisible();
                }
                return true;
        }
        return false;
    }

    @Override
    public SwypeIdRootModel getController() {
        return swypeIdModel;
    }
}
