package com.example.elevador;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    Switch aSwitch;
    View v;
    BluetoothAdapter adapter;
    BluetoothSocket socket;
    BluetoothDevice device;
    ConnectionThread thread;
    public Handler handler;

    public final static String MAC_ADDRESS = "00:13:EF:00:93:E5";
    public final static int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        adapter = BluetoothAdapter.getDefaultAdapter();
        if(!adapter.isEnabled()){
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_ENABLE_BT);
        }
        else{
            initiateBluetoothProcess();
        }

        aSwitch = findViewById(R.id.switchButton);

        aSwitch.setChecked(false);

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(aSwitch.isChecked()){
                    cerrar(v);
                }else{
                    abrir(v);
                }
            }
        });
    }

    public void cerrar(View v){
        Log.i("[BLUETOOTH]", "Attempting to send data");
        if(socket.isConnected() && thread != null){
            String text = "Cerrar";
            thread.write(text.getBytes());
        }
        else{
            Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }

    public void abrir(View v){
        Log.i("[BLUETOOTH]", "Attempting to send data");
        if(socket.isConnected() && thread != null){
            String text = "Abrir";
            thread.write(text.getBytes());
        }
        else{
            Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }

    public void subir(View v){
        Log.i("[BLUETOOTH]", "Attempting to send data");
        if(socket.isConnected() && thread != null){
            String text = "Subir";
            thread.write(text.getBytes());
        }
        else{
            Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }

    public void bajar(View v){
        Log.i("[BLUETOOTH]", "Attempting to send data");
        if(socket.isConnected() && thread != null){
            String text = "Bajar";
            thread.write(text.getBytes());
        }
        else{
            Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }

    public void detener(View v){
        Log.i("[BLUETOOTH]", "Attempting to send data");
        if(socket.isConnected() && thread != null){
            String text = "Detener";
            thread.write(text.getBytes());
        }
        else{
            Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT){
            initiateBluetoothProcess();
        }
    }

    public void initiateBluetoothProcess(){
        if(adapter.isEnabled()){
            device = adapter.getRemoteDevice(MAC_ADDRESS);
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();
                Log.i("[BLUETOOTH]", "Connected to: " + device.getName());
                Toast.makeText(HomeActivity.this, "You have connected to Bluetooth", Toast.LENGTH_LONG).show();
            }
            catch(IOException ioe){
                ioe.printStackTrace();
                try{
                    if(socket != null){
                        socket.close();
                    }
                }
                catch (IOException ioe2){
                    ioe2.printStackTrace();
                }
            }
            Log.i("[BLUETOOTH]", "Creating handler");
            handler = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(Message mssg){
                    if(mssg.what == ConnectionThread.RESPONSE_MESSAGE){
                        String txt = (String)mssg.obj;
                    }
                }
            };
            Log.i("[BLUETOOTH]", "Creating and running Thread");
            thread = new ConnectionThread(socket, handler);
            thread.start();
        }
    }
}
