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
    private static final String CORRECT_SIGNATURE = "2550000000000000000217827752405108254216254103000001131164002233200003005556150152285000000255003320800190000050040705055555251518100550099000005510000500050013500000130519200000000055130513300000000000000000000000000000000505024825425500148027110534713415260160360025500000114431000000161214518060002011285413200000000000555555846011283700008000176212800000015452216136123619560000215722161037139711672127174700001909221610091459118917990178218711911075100000000"; // TODO get correct SRAM PUF
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
         }
        catch(IOException e){
            e.printStackTrace();
            TextView views = (TextView)findViewById(R.id.textView2);
            views.setText("Bluetooth device not found");
        }
        try {
            sendMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        return getHammingDistance(CORRECT_SIGNATURE, candidateSignature) < 0.10;
    }

    public static double getHammingDistance(String sequence1, String sequence2) {
        // Source: http://stackoverflow.com/a/16260973
        char[] s1 = sequence1.toCharArray();
        char[] s2 = sequence2.toCharArray();

        int shorter = Math.min(s1.length, s2.length);
        int longest = Math.max(s1.length, s2.length);

        double result = 0;
        for (int i=0; i<shorter; i++) {
            if (s1[i] != s2[i]) result++;
        }

        result += longest - shorter;

        return result / 100.0;
    }
}
