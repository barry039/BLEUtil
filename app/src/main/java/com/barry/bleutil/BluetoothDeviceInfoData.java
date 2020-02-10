package com.barry.bleutil;

import android.bluetooth.BluetoothDevice;

public class BluetoothDeviceInfoData {
    private BluetoothDevice bluetoothDevice;
    private int rssi;
    private byte[] broadcast_raw_data;

    public BluetoothDeviceInfoData(BluetoothDevice bluetoothDevice, int rssi, byte[] broadcast_raw_data) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
        this.broadcast_raw_data = broadcast_raw_data;
    }

    public BluetoothDeviceInfoData(BluetoothDevice bluetoothDevice, int rssi) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
        this.broadcast_raw_data = broadcast_raw_data;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public int getRssi() {
        return rssi;
    }

    public byte[] getBroadcast_raw_data() {
        return broadcast_raw_data;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public void setBroadcast_raw_data(byte[] broadcast_raw_data) {
        this.broadcast_raw_data = broadcast_raw_data;
    }
}
