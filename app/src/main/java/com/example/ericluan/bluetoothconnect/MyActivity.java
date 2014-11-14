package com.example.ericluan.bluetoothconnect;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.RunnableFuture;

import android.view.*;
import android.widget.*;

public class MyActivity extends Activity {
    private BluetoothAdapter adapter;
    private BluetoothDevice ourDevice;
    private HashSet<BluetoothDevice> devices;
    private BluetoothSocket socket;
    private OutputStream output;
    private InputStream input;
    private static final String OUR_BLUETOOTH_DEVICE = ""; //TODO What was the name of the bluetooth device?
    private static final UUID ID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Might be shady???
    volatile boolean stop;
    byte[] readBuffer;
    int index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void connect(View view){
        if(findBlueToothDevice()){
            if(openBlueToothDevice()){
                this.sendMessage(); //We might need to send message to bluetooth device to get data back depending on implementation.
                this.listen();
            }
        }
    }

    public boolean findBlueToothDevice(){
        adapter = BluetoothAdapter.getDefaultAdapter();
        ourDevice = null;
        if(adapter == null){
            TextView view = (TextView)findViewById(R.id.textView2);
            view.setText("No bluetooth adapter detected");
            return false;
        }
        else if(!adapter.isEnabled()){
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, 0);
        }
        devices = (HashSet)adapter.getBondedDevices();
        for(BluetoothDevice device: devices){
            if(device.getName().equals(OUR_BLUETOOTH_DEVICE)){
                ourDevice = device;
            }
            else
                continue;
        }
        return !(ourDevice == null);
    }

    public boolean openBlueToothDevice(){
        try {
            socket = ourDevice.createRfcommSocketToServiceRecord(ID);
            socket.connect();
            output = socket.getOutputStream();
            input = socket.getInputStream();
            return true;
        }
        catch(IOException e){
            TextView view = (TextView)findViewById(R.id.textView2);
            view.setText("Issue opening bluetooth port");
            return false;
        }

    }
    public void sendMessage(){
        //TODO: Do we need to send a command to the bluetooth device for it to send us the SRAM?
    }
    public void listen(){
        final Handler handler = new Handler();
        final byte delimiter = 10;
        stop = false;
        index = 0;
        readBuffer = new byte[1024];
        Thread thread = new Thread(new Runnable() {
            public void run(){
                while(!Thread.currentThread().isInterrupted() && !stop){
                    try{
                        int dataSize = input.available();
                        byte[] packet = new byte[dataSize];
                        input.read(packet);
                        for(int i = 0; i <dataSize; i ++){
                            if(packet[i] == delimiter){
                                byte[] readableBytes = new byte[index];
                                System.arraycopy(readBuffer, 0, readableBytes, 0, readableBytes.length);
                                String sram = new String(readableBytes, "US-ASCII");
                                boolean correct = correctSRAM(sram);
                                if(correct){
                                    TextView view = (TextView)findViewById(R.id.textView2);
                                    view.setText("This is our bluetooth device!");
                                    Button button = (Button)findViewById(R.id.button);
                                    button.setBackgroundColor(Color.GREEN);
                                }
                                else{
                                    TextView view = (TextView)findViewById(R.id.textView2);
                                    view.setText("Wrong device!");
                                    Button button = (Button)findViewById(R.id.button);
                                    button.setBackgroundColor(Color.RED);
                                }
                                stop = true; 
                            }
                            else{
                                readBuffer[index] = packet[i];
                                index ++;
                            }
                        }
                    }
                    catch(IOException e){
                        stop = true;
                        TextView view = (TextView)findViewById(R.id.textView2);
                        view.setText("Issue reading serial input");
                    }
                }
            }
        });

    }
    public boolean correctSRAM(String s){
        //TODO code HD Calculator (may need to make static final variable for corrrect sram puf)
        return true;
    }
}
