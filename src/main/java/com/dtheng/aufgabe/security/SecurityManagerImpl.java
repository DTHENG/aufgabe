package com.dtheng.aufgabe.security;

import com.dtheng.aufgabe.config.ConfigManager;
import com.dtheng.aufgabe.config.model.AufgabeConfig;
import com.dtheng.aufgabe.security.exception.InvalidPublicKey;
import com.dtheng.aufgabe.security.exception.InvalidSignature;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
@Slf4j
public class SecurityManagerImpl implements SecurityManager {

    private ConfigManager configManager;

    @Inject
    public SecurityManagerImpl(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public Observable<Boolean> verifyRequest(String publicKey, String signature, String request) {
        return configManager.getConfig()
            .flatMap(config -> {
                if ( ! publicKey.equals(config.getPublicKey()))
                    return Observable.error(new InvalidPublicKey());
                return getSignature(request);
            })
            .flatMap(calcSig -> {
                if ( ! calcSig.equals(signature)) {
                    log.info("Returning invalid signature for request, correct signature: \"{}\", body: \"{}\"", calcSig, request);
                    return Observable.error(new InvalidSignature());
                }
                return Observable.just(true);
            });
    }

    @Override
    public Observable<String> getSignature(String data) {
        return configManager.getConfig()
            .map(AufgabeConfig::getPrivateKey)
            .flatMap(privateKey -> getHmacSha256(privateKey + data, privateKey))
            .map(String::toLowerCase)
            .flatMap(baseHash -> getHmacSha256("admin", baseHash))
            .map(String::toLowerCase);
    }

    private Observable<String> getHmacSha256(String data, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return getDataAsBytes(data)
                .flatMap(asBytes -> toHex(sha256_HMAC.doFinal(asBytes)));
        } catch (Exception e) {
            return Observable.error(e);
        }
    }

    private Observable<byte[]> getDataAsBytes(String data) {
        try {
            return Observable.just(data.getBytes("UTF-8"));
        } catch (Exception e) {
            return Observable.error(e);
        }
    }

    private static Observable<String> toHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return Observable.just(new String(hexChars));
    }
}
