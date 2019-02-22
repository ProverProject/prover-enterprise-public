package io.prover.clapperboard.enterprise.model;

import android.app.Activity;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;

import io.prover.clapperboard.ClapperboardMissionModel;
import io.prover.common.controller.RootModel;
import io.prover.common.enterprise.transport.EnterpriseTransportModel;
import io.prover.common.transport.OrderType;

import static io.prover.common.Const.ENTERPRISE_SERVER_URI;


/**
 * Created by babay on 17.11.2017.
 */

@MainThread
public class ClapperboardRootModel extends RootModel<EnterpriseTransportModel, ClapperboardMissionModel> {

    private ClapperboardRootModel(Activity activity, EnterpriseTransportModel transport, ClapperboardMissionModel missionModel) {
        super(activity, transport, missionModel);
    }

    @MainThread
    public static ClapperboardRootModel create(Activity activity) {
        String uri = PreferenceManager.getDefaultSharedPreferences(activity).getString(ENTERPRISE_SERVER_URI, null);
        EnterpriseTransportModel transport = new EnterpriseTransportModel(Uri.parse(uri), 0, OrderType.QrCode);
        ClapperboardMissionModel missionModel = new ClapperboardMissionModel(transport, activity);
        return new ClapperboardRootModel(activity, transport, missionModel);
    }
}
