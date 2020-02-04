package com.example.elevador;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback{
    private CancellationSignal cancellationSignal;
    private Context appContext;


    public FingerprintHandler(Context context){
        this.appContext = context;
    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject){
        cancellationSignal = new CancellationSignal();
        if(ActivityCompat.checkSelfPermission(appContext, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString){
        Toast.makeText(appContext, "Authentication error\n" + errString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString){
        Toast.makeText(appContext, "Authentication help\n" + helpString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed(){
        Toast.makeText(appContext, "Authentication failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result){
        Toast.makeText(appContext, "Authentication succeeded", Toast.LENGTH_LONG).show();
        this.update(true);
    }

    public void update(Boolean success){
        Button button = (Button) ((Activity)appContext).findViewById(R.id.button);
        if(success){
            button.setEnabled(true);
        }
    }
}