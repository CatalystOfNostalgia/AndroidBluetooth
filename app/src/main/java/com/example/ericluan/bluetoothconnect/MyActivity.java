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
    private Collection<BluetoothDevice> devices;
    private BluetoothSocket socket;
    private OutputStream output;
    private InputStream input;
    private static final String OUR_BLUETOOTH_DEVICE = "BadAssTechies";
    private static final String CORRECT_SIGNATURE = "255023425418720122312322324712719098167811234821782775240510825424325410300000127116400223320000300555615015228500000025310332270019000005160407050555552515181005500990000055100005005500135000001305109000000000551305215000000000000000000000000000000005050248254255002310271105347134152601603600255000001144310000001612130194000020112854132001460000000055555584601022500000000176212800000015452216136123619560000215722161037139711672127174700001909221610091459118917990178221612201075100000000"; // TODO get correct SRAM PUF
    volatile boolean stop;
    volatile boolean right;
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

    public void connect(View view) {
        try {
            findBlueToothDevice();
            openBlueToothDevice();
            TextView views = (TextView)findViewById(R.id.textView2);
            views.setText("Bluetooth device connected successfully.");
            Button button = (Button)findViewById(R.id.button2);
            button.setEnabled(true);

         }
        catch(IOException e){
            e.printStackTrace();
            TextView views = (TextView)findViewById(R.id.textView2);
            views.setText("Bluetooth device failed to connect.");
        }

    }
    public void authenticate(View view){
        try {
            sendMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView views = (TextView)findViewById(R.id.textView2);
        views.setText("In Progress...");
        while(!stop) {
        }
        if(right){
            views.setText("Authentication successful!");
            view.setBackgroundColor(Color.GREEN);
        }
        else{
            views.setText("Authentication failed!");
            view.setBackgroundColor(Color.RED);
        }
        stop = false;
    }
    public void findBlueToothDevice(){
        adapter = BluetoothAdapter.getDefaultAdapter();
        ourDevice = null;
        if(adapter == null){
            TextView view = (TextView)findViewById(R.id.textView2);
            view.setText("No bluetooth adapter detected");
            return;
        }
        else if(!adapter.isEnabled()){
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, 0);
        }
        devices = adapter.getBondedDevices();
        for(BluetoothDevice device: devices){
            if(device.getName().equals(OUR_BLUETOOTH_DEVICE)){
                ourDevice = device;
                break;
            }
            else
                continue;
        }
        return;
    }

    public void openBlueToothDevice() throws IOException {
        UUID id = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        socket = ourDevice.createRfcommSocketToServiceRecord(id);
        socket.connect();
        output = socket.getOutputStream();
        input = socket.getInputStream();
        listen();
        return;

        /*
        catch(IOException e){
            TextView view = (TextView)findViewById(R.id.textView2);
            view.setText("Issue opening bluetooth port");
            e.printStackTrace();
            return false;
        }*/

    }
    public void sendMessage() throws IOException{
        String msg = "$";
        msg += "\n";
        output.write(msg.getBytes());
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
                                right = correctSRAM(sram);
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
        thread.start();

    }
    public boolean correctSRAM(String candidateSignature){
        return LevenshteinDistance(CORRECT_SIGNATURE, candidateSignature) < 40;
    }

    /**
     * Courtesy of http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
     * @param s0 string 1
     * @param s1 String 2
     * @return
     */
    public int LevenshteinDistance (String s0, String s1) {
        int len0 = s0.length() + 1;
        int len1 = s1.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamicaly computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for(int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (s0.charAt(i - 1) == s1.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }
}
