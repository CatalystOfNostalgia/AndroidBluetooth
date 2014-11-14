package com.example.ericluan.bluetoothconnect;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.*;
import java.util.*;
import android.view.*;
import android.widget.*;
public class MyActivity extends Activity {
    public BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    public HashSet<BluetoothDevice> devices;
    public static final String bluetoothMacAddress = "00:06:66:69:86:D4";
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
        devices = (HashSet<BluetoothDevice>) adapter.getBondedDevices();
        BluetoothDevice board = null;
        for(BluetoothDevice device : devices){
            if(device.getAddress().equals(bluetoothMacAddress)){
                board = device;
                break;
            }
        }
        if(board == null){
            view.setBackgroundColor(Color.RED);
            TextView textbox = (TextView)findViewById(R.id.textView2);
            textbox.setText("Board not connected, go to Settings and connect board");
        }
        else{
            view.setBackgroundColor(Color.GREEN);
            board.

        }
    }

    public boolean correctSRAM(String s){
        return true;
    }
}
