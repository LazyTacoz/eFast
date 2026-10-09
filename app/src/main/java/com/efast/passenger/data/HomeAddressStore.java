package com.efast.passenger.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.efast.passenger.data.model.HomeAddress;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Keeps the driver's home address on the phone, encrypted with an AES-256-GCM key held in
 * the Android Keystore (the key never leaves the device's secure hardware where available).
 * The address is sensitive personal data under the DPDP Act: nothing here is sent to riders.
 */
public final class HomeAddressStore {

    private static final String PREFS = "home_ride_secure";
    private static final String KEY_HOME = "home";
    private static final String KEY_ALIAS = "efast_home_address";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;

    private final SharedPreferences prefs;

    HomeAddressStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Returns null when no home is saved, or it can no longer be decrypted (e.g. restored to a new phone). */
    public HomeAddress load() {
        String stored = prefs.getString(KEY_HOME, null);
        if (stored == null) return null;
        try {
            JSONObject json = new JSONObject(decrypt(stored));
            return new HomeAddress(json.getString("line"), json.getString("area"),
                    json.getDouble("lat"), json.getDouble("lng"));
        } catch (GeneralSecurityException | JSONException | IllegalArgumentException e) {
            return null;
        }
    }

    public void save(HomeAddress home) throws GeneralSecurityException {
        String json;
        try {
            json = new JSONObject()
                    .put("line", home.addressLine)
                    .put("area", home.areaName)
                    .put("lat", home.lat)
                    .put("lng", home.lng)
                    .toString();
        } catch (JSONException e) {
            throw new GeneralSecurityException(e);
        }
        prefs.edit().putString(KEY_HOME, encrypt(json)).apply();
    }

    public void clear() {
        prefs.edit().remove(KEY_HOME).apply();
    }

    private String encrypt(String plain) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP)
                + ":" + Base64.encodeToString(cipherText, Base64.NO_WRAP);
    }

    private String decrypt(String stored) throws GeneralSecurityException {
        String[] parts = stored.split(":", 2);
        if (parts.length != 2) throw new GeneralSecurityException("Malformed value");
        byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
        byte[] cipherText = Base64.decode(parts[1], Base64.NO_WRAP);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    }

    private SecretKey getOrCreateKey() throws GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        try {
            keyStore.load(null);
        } catch (IOException e) {
            throw new GeneralSecurityException(e);
        }
        Key existing = keyStore.getKey(KEY_ALIAS, null);
        if (existing instanceof SecretKey) return (SecretKey) existing;

        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build());
        return generator.generateKey();
    }
}
