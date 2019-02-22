package io.prover.common;

/**
 * Created by babay on 14.01.2018.
 */

public interface Const {

    String ARG_ORDER_REQUEST = "otask";
    String ARG_ORDER_RESULT = "oresult";


    String ENTERPRISE_SERVER_URI = "eserver";

    int REQUEST_CODE_FOR_REQUEST_PERMISSIONS = 1010;
    int REQUEST_CODE_FOR_IMPORT_WALLET = 1001;

    String SERVER_URL = "http://mvp.prover.io/cgi-bin/";

    String SITE_URL = "https://production-dev.prover.io/";
    String RESET_PASSWORD_URL = "https://production.prover.io/reset-password";

    String TAG = "io.prover.";

    String ARG_HAS_PIN_LOGIN = "hasPinLogin";
    String ARG_TUTORIAL_SHOWN = "tutorialShown";
    String ARG_NEXT_NONCE = "nextNonce";
    String ARG_LOGIN = "login";
    String ARG_PREFERRED_SIG_PROVIDER = "spr";

    /**
     * Preferences storage exclusively for nonce
     */
    String NONCE_STORAGE = "nonceStorage";

    /**
     * preferences storage for data that should not be backed up
     */
    String LOCAL_STORAGE = "localStorage";

    String[] FAKE_SWYPES = {/*"5785124", "5965365", "5254569", "5869654", "5351547", "5248652", "5896321", "5474145",*/ "*12345678"};
}
