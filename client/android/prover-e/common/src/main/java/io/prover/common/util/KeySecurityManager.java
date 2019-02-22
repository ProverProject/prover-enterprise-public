package io.prover.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;

import io.prover.common.BuildConfig;
import io.prover.common.Const;

import static io.prover.common.Const.ARG_PREFERRED_SIG_PROVIDER;
import static io.prover.common.Const.LOCAL_STORAGE;
import static io.prover.common.Const.NONCE_STORAGE;
import static io.prover.common.Const.TAG;

public class KeySecurityManager {
    private static final String PROVIDER = "AndroidKeyStore";
    private static final String ALIAS = "Prover";
    private static String preferredSignatureProvider = null;
    private final Context context;
    private PrivateKeyEntry keyEntry;

    public KeySecurityManager(Context context) {
        this.context = context.getApplicationContext();
        keyEntry = KeyStore.loadCreateKey(context, ALIAS, PROVIDER);
        preferredSignatureProvider = context.getSharedPreferences(LOCAL_STORAGE, Context.MODE_PRIVATE).getString(ARG_PREFERRED_SIG_PROVIDER, null);
    }

    private void ensureHasKey() {
        if (keyEntry == null)
            keyEntry = KeyStore.loadCreateKey(context, ALIAS, PROVIDER);
    }

    /**
     * @param message to sign
     * @return base64 ( Signature (message))
     */
    public String sign(String message) {
        ensureHasKey();
        if (keyEntry == null) {
            return null;
        }

        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        if (preferredSignatureProvider != null) { // try sign with preferred provider;
            try {
                return trySign(data, preferredSignatureProvider, keyEntry.getPrivateKey());
            } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | NoSuchProviderException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "sign: " + e.getMessage(), e);
                }
                preferredSignatureProvider = null;
            }
        }

        // find provider to sign
        Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            try {
                String result = trySign(data, provider.getName(), keyEntry.getPrivateKey());
                setPreferredSignatureProvider(provider);
                return result;
            } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | NoSuchProviderException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "sign: " + e.getMessage(), e);
                }
            }
        }

        // blame system -- it can't sign with our keys
        StringBuilder builder = new StringBuilder();
        for (Provider provider : providers) {
            builder.append(provider.getName()).append(", ");
        }

        throw new RuntimeException("Can't find provider to sign :(, providers: " + builder.toString());
    }

    /**
     * @return Base64 (X.509 (public key) )
     */
    public String getPublicKeyEncoded() {
        ensureHasKey();
        if (keyEntry == null) {
            return null;
        }
        byte[] encodedPublicKey = keyEntry.getCertificate().getPublicKey().getEncoded();
        return Base64.encodeBase64(encodedPublicKey);
    }

    private void setPreferredSignatureProvider(Provider provider) {
        preferredSignatureProvider = provider.getName();
        context.getSharedPreferences(LOCAL_STORAGE, Context.MODE_PRIVATE).edit()
                .putString(ARG_PREFERRED_SIG_PROVIDER, provider.getName()).apply();
    }

    public String nextNonce() {
        SharedPreferences prefs = context.getSharedPreferences(NONCE_STORAGE, Context.MODE_PRIVATE);
        int nextNonce = prefs.getInt(Const.ARG_NEXT_NONCE, 0);
        prefs.edit().putInt(Const.ARG_NEXT_NONCE, nextNonce + 1).commit();
        return Integer.toString(nextNonce);
    }

    private String trySign(byte[] data, String provider, PrivateKey key) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature s = Signature.getInstance("SHA256withECDSA", provider);
        s.initSign(key);
        s.update(data);
        byte[] signature = s.sign();
        return Base64.encodeBase64(signature);
    }
}
