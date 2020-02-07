package com.example.elevador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private static final String KEY_NAME = "my_key";
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;
    Button button;
    FingerprintHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("AQUI!!!!");

        fingerprintManager = (FingerprintManager)getSystemService(FINGERPRINT_SERVICE);
        keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);

        button = findViewById(R.id.button);

        //button.setEnabled(false);

        if(!keyguardManager.isKeyguardSecure()){
            Toast.makeText(this, "Lock screen security is not enabled", Toast.LENGTH_LONG).show();
            button.setEnabled(false);
            return;
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Fingerprint authentication permission is not enabled", Toast.LENGTH_LONG).show();
            button.setEnabled(false);
            return;
        }
        if(!fingerprintManager.hasEnrolledFingerprints()){
            Toast.makeText(this, "Register at least one fingerprint in settings", Toast.LENGTH_LONG).show();
            button.setEnabled(false);
            return;
        }
        System.out.println("AQUI!!!!");
        generateKey();
        System.out.println("AQUI!!!!");
        if(initCipher()) {
            cryptoObject = new FingerprintManager.CryptoObject(cipher);
            FingerprintHandler handler = new FingerprintHandler(this);
            handler.startAuth(fingerprintManager, cryptoObject);
            //button.setEnabled(true);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity();
            }
        });
    }

    public void openActivity(){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    protected void generateKey(){
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC).setUserAuthenticationRequired(true).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build());
            keyGenerator.generateKey();
            //button.setEnabled(true);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean initCipher(){
        try{
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            keyStore.load(null);
            SecretKey key = (SecretKey)keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //button.setEnabled(true);
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }
}