package com.ian.gyremote.gyremote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {

    private Button On,Off,Visible,list;
    private Set<BluetoothDevice> pairedDevices;
    private static final String TAG = "bluetooth";
    private SensorManager sManager;


    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static String address = "00:6A:8E:16:C9:47";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        On = (Button)findViewById(R.id.button1);
        Off = (Button)findViewById(R.id.button2);
        Visible = (Button)findViewById(R.id.button3);
        list = (Button)findViewById(R.id.button4);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void on(View view){
        if (!btAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(),"Turned on", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void list(View view){
        pairedDevices = btAdapter.getBondedDevices();

        ArrayList list = new ArrayList();
        for(BluetoothDevice dev : pairedDevices) {
            list.add(dev.getName());
            address = dev.getAddress();
        }

        Toast.makeText(getApplicationContext(),"Connected",
                Toast.LENGTH_SHORT).show();
    }

    public void off(View view){
        btAdapter.disable();
        Toast.makeText(getApplicationContext(),"Turned off" ,
                Toast.LENGTH_LONG).show();
    }

    public void goForward(View view){
        sendData("g");
    }

    public void brake(View view){
        sendData("b");
    }

    public void reverse(View view) {
        sendData("r");
    }

    public void goLeft(View view) {
        sendData("l");
    }

    public void goRight(View view) {
        sendData("i");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            e.printStackTrace();
        }

        btAdapter.cancelDiscovery();

        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }


        //get a hook to the sensor service
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                Toast.makeText(this, "Error in outStream.flush()", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, address + "" + message);

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }


    public void onAccuracyChangedListener(SensorEvent event) {

    }

}