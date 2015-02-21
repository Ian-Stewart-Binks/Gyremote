package com.ian.gyremote.gyremote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.*;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.io.InputStream;
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
import android.widget.*;
import android.graphics.Color;

public class MainActivity extends Activity implements SensorEventListener {

    private Button something, forward, reverse, list, brake, right, left, light;
    private ImageView frontIndicator, backIndicator, leftIndicator, rightIndicator;
    private Set<BluetoothDevice> pairedDevices;
    private static final String TAG = "bluetooth";
    private SensorManager sManager;
    private TextView tv;
    public static final int MESSAGE_READ = 1;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            final int what = msg.what;
            switch(what) {
                case MESSAGE_READ: tv.setText(msg.toString()); break;
            }
        }
    };

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private InputStream inputStream = null;

    private OutputStream outStream = null;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC Address of bluetooth shield.
    private static String address = "00:6A:8E:16:C9:47";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frontIndicator = (ImageView)findViewById(R.id.imageView3);
        something = (Button)findViewById(R.id.button1);
        forward = (Button)findViewById(R.id.button2);
        reverse = (Button)findViewById(R.id.button3);
        list = (Button)findViewById(R.id.button4);
        brake = (Button)findViewById(R.id.button5);
        light = (Button)findViewById(R.id.button7);
        rightIndicator = (ImageView)findViewById(R.id.imageView2);
        leftIndicator = (ImageView)findViewById(R.id.imageView4);
        backIndicator = (ImageView)findViewById(R.id.imageView);
        tv = (TextView)findViewById(R.id.textView);

        btAdapter = BluetoothAdapter.getDefaultAdapter();


        new Thread (new Runnable() {
            @Override
            public void run() {
                final byte[] buffer = new byte[1024];
                int bytes;

                while (true) {

                    try {
                        if (inputStream != null && inputStream.available() > 0) {
                            bytes = inputStream.read(buffer);
                            Log.d(TAG, buffer.toString());
                            mHandler.post(new Runnable() {
                                public void run() {
                                    try {
                                        String decoded = new String(buffer, "UTF-8");
                                        if (decoded.charAt(0) == '0') {
                                            frontIndicator.setBackgroundColor(Color.BLUE);
                                        } else if (decoded.charAt(0) == '1') {
                                            frontIndicator.setBackgroundColor(Color.RED);
                                        } else if (decoded.charAt(0) == '2') {
                                            backIndicator.setBackgroundColor(Color.BLUE);
                                        } else if (decoded.charAt(0) == '3') {
                                            backIndicator.setBackgroundColor(Color.RED);
                                        } else if (decoded.charAt(0) == '4') {
                                            rightIndicator.setBackgroundColor(Color.BLUE);
                                        } else if (decoded.charAt(0) == '5') {
                                            rightIndicator.setBackgroundColor(Color.RED);
                                        } else if (decoded.charAt(0) == '6') {
                                            leftIndicator.setBackgroundColor(Color.BLUE);
                                        } else if (decoded.charAt(0) == '7') {
                                            leftIndicator.setBackgroundColor(Color.RED);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void on(View view){
        sendData("A");
    }

    public void lightsOn(View view) {
        sendData("o");
    }

    public void disable(View view) {
        sendData("9");
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

    public void goForward(){
        sendData("g");
    }

    public void brake(View view){
        sendData("b");
    }

    public void reverse() {
        sendData("r");
    }

    public void goLeft() {
        sendData("l");
    }

    public void goRight() {
        sendData("i");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void run2() {
        byte[] buffer = new byte[1024];
        int bytes; 
        while (true) {
            try {
                if (inputStream != null) {
                    bytes = inputStream.read(buffer);
                    tv.setText(bytes + "");
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set up connection to car.
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

        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sManager.registerListener(this,
                                  sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                                  SensorManager.SENSOR_DELAY_FASTEST);

        try {
            outStream = btSocket.getOutputStream();
            inputStream = btSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        try {
            final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
            return (BluetoothSocket) m.invoke(device, MY_UUID);
        } catch (Exception e) {
            e.printStackTrace();
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
        currentChar = message;
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, address + "" + message);

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int currentRoll = 0;
    int currentPitch = 0;
    String currentChar = "";

    @Override
    public void onSensorChanged(SensorEvent event) {
        char memoryVal = '0';
        tv.setText("" + (event.values[2]));


        if (event.values[2] > 30) {
            goLeft();
        } else if (event.values[2] < -30) {
            goRight();
        } else if (event.values[1] > 10) {
            goForward();
        } else if (event.values[1] < -10) {
            reverse();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

}
