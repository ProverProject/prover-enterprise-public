package io.prover.clapperboard.enterprise;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.prover.clapperboard.enterprise.model.ClapperboardRootModel;
import io.prover.clapperboard.enterprise.viewholder.ControlsViewHolder;
import io.prover.common.IMainActivity;
import io.prover.common.permissions.PermissionManager;
import io.prover.common.view.ScreenLogger;

public class MainActivity extends AppCompatActivity implements IMainActivity<ClapperboardRootModel> {

    ClapperboardRootModel controller;
    private ScreenLogger loggerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        controller = ClapperboardRootModel.create(this);
        new ControlsViewHolder(this, controller);
        loggerView = new ScreenLogger(findViewById(R.id.contentRoot), R.id.offer, R.id.balanceContainer);
        loggerView.setVisible(false);
        controller.mission.logger.setView(loggerView);
        findViewById(R.id.helpLink).setOnLongClickListener(this::toggleScreenLog);
    }

    private boolean toggleScreenLog(View view) {
        if (loggerView != null) {
            loggerView.toggleVisible();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        new Handler().post(() -> PermissionManager.onPermissionRequestDone(this, requestCode, permissions, grantResults));
    }

    @Override
    public ClapperboardRootModel getController() {
        return controller;
    }

    @Override
    @UiThread
    public void onBackPressed() {
        super.onBackPressed();
        if (controller != null) {
            controller.mission.onFinishedMainTask();
            controller.mission.clearStoredState(this);
        }
    }
}
