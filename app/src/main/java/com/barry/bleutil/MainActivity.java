package com.barry.bleutil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final int REQUEST_PERMISSION_LOCATION = 1001;
    public final int REQUEST_ENABLE_BT = 1002;

    private BLEUtil bleUtil;

    private RecyclerView recyclerView;

    private BleListAdapter bleListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.main_recycleview);
        init();
    }

    private void init() {
        bleUtil = new BLEUtil(getApplicationContext(),scanDeviceCallBack,readDataCallBack);
        bleListAdapter = new BleListAdapter(this, new BleListAdapter.BLEClickListener() {
            @Override
            public void onClicked(BluetoothDeviceInfoData bluetoothDeviceInfoData) {
                bleUtil.connectDevice(bluetoothDeviceInfoData.getBluetoothDevice());
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(bleListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkBLEPermission();
    }

    private BLEUtil.BluetoothInterface.ReadDataCallBack readDataCallBack = new BLEUtil.BluetoothInterface.ReadDataCallBack() {
        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected(BluetoothDevice bluetoothDevice) {

        }
    };

    private boolean update_enable = true;

    private BLEUtil.BluetoothInterface.ScanDeviceCallBack scanDeviceCallBack = new BLEUtil.BluetoothInterface.ScanDeviceCallBack() {
        @Override
        public void ScanResult(List<BluetoothDeviceInfoData> bluetoothDeviceInfoDatas) {
            //scaned device
            if(update_enable)
            {
                bleListAdapter.setDataSet(bluetoothDeviceInfoDatas);
                update_enable = false;
                new Handler(getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        update_enable = true;
                    }
                },3000);
            }
        }
    };



    private void startScan()
    {
        bleUtil.scanDevice(true);
    }

    private void stopScan()
    {
        bleUtil.scanDevice(false);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_LOCATION)
        {
            boolean granted = true;
            for(String s : permissions)
            {
                if(!s.equals(PackageManager.PERMISSION_GRANTED))
                {
                    granted = false;
                }
            }
            if(granted)
            {
                //got all permission
                startScan();
            }
            else
            {
                //some permission not granted
                checkLocationPermission();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT)
        {
            checkBLEPermission();
        }
    }

    private void checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //permission granted.
            startScan();
        }
        else
        {
            //no permission.
            //request permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
        }
    }


    /*
        ============Bluetooth Permission==============
         */
    private void checkBLEPermission() {
        if (isBleSupported()) {
            // Ensures Bluetooth is enabled on the device
            BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter btAdapter = btManager.getAdapter();
            if (btAdapter.isEnabled()) {
                // Prompt for runtime permission
                checkLocationPermission();
            }
            else
            {
                //request ble
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            //The device no ble feature.
            Toast.makeText(this, "Device is not support Bluetooth Low Energy Function", Toast.LENGTH_LONG).show();
            System.exit(1);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScan();
    }

    private boolean isBleSupported() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleUtil.closeDevice();
    }
}
