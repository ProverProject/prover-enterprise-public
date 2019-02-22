package io.prover.common.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.ECKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import static io.prover.common.Const.TAG;

public class KeyStore {


    public static PrivateKeyEntry loadCreateKey(Context context, String alias, String provider) {
        try {
            PrivateKeyEntry entry = loadKey(alias, provider);
            if (entry == null) {
                createKey(alias, provider, context);
                entry = loadKey(alias, provider);
            }
            if (entry == null) {
                Log.e(TAG, "loadCreateKey: KeyPair does no exist after key generation", new Exception());
                return null;
            }
            return entry;
        } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            Log.e(TAG, "loadCreateKey: " + e.getMessage(), e);
            return null;
        }
    }

    private static PrivateKeyEntry loadKey(String alias, String provider) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableEntryException {
        java.security.KeyStore ks = java.security.KeyStore.getInstance(provider);
        ks.load(null);
        List<String> aliases = Collections.list(ks.aliases());

        if (!aliases.contains(alias)) {
            return null;
        }
        java.security.KeyStore.Entry entry = ks.getEntry(alias, null);
        if (!(entry instanceof PrivateKeyEntry)) {
            Log.e(TAG, "Not an instance of a PrivateKeyEntry");
            return null;
        }
        PrivateKeyEntry keyEntry = (PrivateKeyEntry) entry;
        if (!(keyEntry.getPrivateKey() instanceof ECKey)) {
            Log.e(TAG, "Not an instance of a ECKey");
            return null;
        }
        return keyEntry;
    }

    public static KeyPair createKey(String alias, String provider, Context context) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        if (Build.VERSION.SDK_INT >= 23) {
            return generateKey23(alias, provider);
        } else
            return geterateKey(alias, context, provider);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static KeyPair generateKey23(String alias, String provider) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC, provider);

        kpg.initialize(new KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256,
                        KeyProperties.DIGEST_SHA512)
                .build());

        return kpg.generateKeyPair();
    }

    private static KeyPair geterateKey(String alias, Context context, String provider) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 40);

        @SuppressLint("WrongConstant")
        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setKeyType("EC")
                .setSubject(new X500Principal("CN=Prover user"))
                .setSerialNumber(BigInteger.valueOf(123456789L))
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                "RSA", provider);
        kpg.initialize(spec);
        return kpg.generateKeyPair();
    }
}
