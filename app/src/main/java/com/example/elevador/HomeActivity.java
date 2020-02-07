package com.example.elevador;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    BluetoothAdapter adapter;
    BluetoothSocket socket;
    BluetoothDevice device;
    ConnectionThread thread;
    public Handler handler;
    TextView hora;
    Properties horas;

    public final static String MAC_ADDRESS = "00:13:EF:00:93:E5";
    public final static int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final String HOURS_FILENAME = "hours.xml";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        hora = findViewById(R.id.hora);
        horas = new Properties();

        loadHours();

        lights();

        adapter = BluetoothAdapter.getDefaultAdapter();
        if(!adapter.isEnabled()){
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_ENABLE_BT);
        }
        else{
            initiateBluetoothProcess();
        }
    }

    private void loadHours(){
        try{
            FileInputStream fis = openFileInput(HOURS_FILENAME);
            horas.loadFromXML(fis);
            fis.close();
        }catch(FileNotFoundException fnfe){
            horas.setProperty("inicio","18:00");
            horas.setProperty("final","20:00");
            try{
                FileOutputStream fos = openFileOutput(HOURS_FILENAME, Context.MODE_PRIVATE);
                horas.storeToXML(fos, null);
                fos.close();
            }catch (IOException ioe){
                Toast.makeText(this, "No se puede guardar las horas.", Toast.LENGTH_LONG).show();
            }
        }catch (IOException ioe){
            Toast.makeText(this, "No se puede leer las horas.", Toast.LENGTH_LONG).show();
        }
    }

    public void handleTime(View v){
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR_OF_DAY);
        int MINUTE = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                String timeString = "Hora: " + hour + " Minutos: " + minute;
                hora.setText(timeString);
                horas.setProperty("inicio", hour + ":" + minute);
                saveHour();
            }
        }, HOUR, MINUTE, true);
        timePickerDialog.show();
    }

    public void saveHour(){
        try{
            FileOutputStream fos = openFileOutput(HOURS_FILENAME, Context.MODE_PRIVATE);
            horas.storeToXML(fos, null);
            fos.close();
        }catch (IOException ioe){
            Toast.makeText(this, "No se puede guardar las horas.", Toast.LENGTH_LONG).show();
        }
    }

    public void handleTime2(View v){
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR_OF_DAY);
        int MINUTE = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                String timeString = "Hora: " + hour + " Minutos: " + minute;
                hora.setText(timeString);
                horas.setProperty("final", hour + ":" + minute);
                saveHour();
            }
        }, HOUR, MINUTE, true);
        timePickerDialog.show();
    }

    public void lights(){
        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int minutes = rightNow.get(Calendar.MINUTE);
        String current = hour + ":" + minutes;
        if(current == horas.getProperty("inicio")){
            if(socket.isConnected() && thread != null){
                String text = "on";
                thread.write(text.getBytes());
            }
        }else if(current == horas.getProperty("final")){
            if(socket.isConnected() && thread != null){
                String text = "off";
                thread.write(text.getBytes());
            }
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
