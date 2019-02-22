package io.prover.swypeid.enterprise.model;

import android.app.Activity;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;

import io.prover.common.controller.RootModel;
import io.prover.common.enterprise.transport.EnterpriseTransportModel;
import io.prover.common.transport.OrderType;
import io.prover.swypeid.model.SwypeIdMission;

import static io.prover.common.Const.ENTERPRISE_SERVER_URI;
import static io.prover.swypeid.enterprise.Const.KEY_USE_FAST_SWYPECODE;

/**
 * Created by babay on 17.11.2017.
 */

@MainThread
public class SwypeIdRootModel extends RootModel<EnterpriseTransportModel, SwypeIdMission> {

    private SwypeIdRootModel(Activity activity, EnterpriseTransportModel transport, SwypeIdMission mission) {
        super(activity, transport, mission);
        mission.camera.setRootModel(this);
    }

    @MainThread
    public static SwypeIdRootModel create(Activity activity) {
        String uri = PreferenceManager.getDefaultSharedPreferences(activity).getString(ENTERPRISE_SERVER_URI, null);
        EnterpriseTransportModel transport = new EnterpriseTransportModel(Uri.parse(uri), 0, OrderType.SwypeFast);
        SwypeIdMission mission = new SwypeIdMission(activity, transport);
        return new SwypeIdRootModel(activity, transport, mission);
    }

    @Override
    public void onResume(Activity activity) {
        boolean useFastSwypeCode = PreferenceManager.getDefaultSharedPreferences(activity)
                .getBoolean(KEY_USE_FAST_SWYPECODE, true);
        transport.setOrderType(useFastSwypeCode ? OrderType.SwypeFast : OrderType.SwypeFull);
        super.onResume(activity);
    }
}