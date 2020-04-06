package com.barry.bleutil.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.barry.bleutil.BluetoothDeviceInfoData;

import java.util.ArrayList;
import java.util.List;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static com.barry.bleutil.bluetooth.ConvertUtil.byteTohex;
import static com.barry.bleutil.bluetooth.ConvertUtil.extractBytes;

public class BluetoothManager extends BluetoothGattCallback implements BluetoothAdapter.LeScanCallback{

    private Context context;

    private BluetoothAdapter btAdapter;

    private BluetoothGatt bluetoothGatt = null;

    private BluetoothDevice connect_bluetoothDevice = null;

    private BluetoothInterface.ConnectionCallback connectionCallback;

    private BluetoothInterface.ScanDeviceCallBack scanDeviceCallBack;

    private BluetoothConfig bluetoothConfig;

    private String connectAddress = "";

    private boolean is_connect = false;

    /*
        ================================================
        bluetooth init func
        ================================================
    */


    public BluetoothManager(Context context, BluetoothInterface.ScanDeviceCallBack scanDeviceCallBack, BluetoothInterface.ConnectionCallback connectionCallback)
    {
        bluetoothConfig = new BluetoothConfig();
        this.scanDeviceCallBack = scanDeviceCallBack;
        this.connectionCallback = connectionCallback;
        this.context = context;
        getBTAdapter();
    }

    public void getBTAdapter(){
        android.bluetooth.BluetoothManager btManager = (android.bluetooth.BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter= (BluetoothAdapter) btManager.getAdapter();
    }

    /*
        ================================================
        bluetooth scan func
        ================================================
    */

    private boolean isScanning = false;

    private boolean isOpenScan = true;

    private int next_Scan_state = -1; //-1 無預存狀態; 0 預存下個狀態為掃描; 1預存下個狀態為停止掃描;

    public synchronized void scanDevice(boolean enable)
    {
        if(isOpenScan)
        {
            if(enable)
            {
                startScan();
            }else
            {
                stopScan();
            }
            startBTAdapterIntervalTime();
        }else
        {
            if(enable)
            {
                next_Scan_state = 0;
            }else
            {
                next_Scan_state = 1;
            }
        }
    }

    //間隔調用時間 避免bluetoothadapter instance調用失敗
    private void startBTAdapterIntervalTime()
    {
        next_Scan_state = -1;
        isOpenScan = false;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                isOpenScan = true;

                if(next_Scan_state != -1)
                {
                    if(next_Scan_state == 0)
                    {
                        scanDevice(true);
                    }else if(next_Scan_state == 1)
                    {
                        scanDevice(false);
                    }
                }
            }
        },1000);
    }

    private synchronized void startScan() {
        if(!isScanning)
        {
            if(btAdapter != null)
            {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                            // only for gingerbread and newer versions
                            final ScanSettings mScanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).setReportDelay(0).build();
                            ScanFilter.Builder builder = new ScanFilter.Builder();
                            final ScanFilter filter = builder.build();
                            List<ScanFilter> filters = new ArrayList<>();
                            filters.add(filter);
                            btAdapter.getBluetoothLeScanner().startScan(filters,mScanSettings,mBLEScan);
                        }else
                        {
                            btAdapter.startLeScan(BluetoothManager.this);
                        }
                        isScanning = true;
                    }
                });

            }

        }
    }

    private synchronized void stopScan()
    {
        if(isScanning)
        {
            if(btAdapter != null)
            {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        btAdapter.stopLeScan(BluetoothManager.this);
                        isScanning = false;
                    }
                });
            }
        }
    }

    private ScanCallback mBLEScan = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            final BluetoothDevice bluetoothDevice = result.getDevice();
            final int rs = result.getRssi();
            final byte[] raw_data = result.getScanRecord().getBytes();
            parseScanRecord(raw_data);
            if(bluetoothDevice.getName() != null)
            {
                if(!isContain(bluetoothDevice.getAddress(),rs,raw_data))
                {

                    bluetoothDeviceInfoDataList.add(new BluetoothDeviceInfoData(bluetoothDevice,rs,raw_data));
                }
            }
            scanDeviceCallBack.ScanResult(bluetoothDeviceInfoDataList);
        }

    };

    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private List<BluetoothDeviceInfoData> bluetoothDeviceInfoDataList = new ArrayList<>();
    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int rssi,byte[] broadcast) {
        //received peripheral instance
        final BluetoothDevice device = bluetoothDevice;

        final int rs = rssi;

        final byte[] raw_data = broadcast;

        parseScanRecord(broadcast);
        if(device.getName() != null)
        {
            if(!isContain(device.getAddress(),rs,raw_data))
            {

                bluetoothDeviceInfoDataList.add(new BluetoothDeviceInfoData(device,rssi,broadcast));
            }
        }
        scanDeviceCallBack.ScanResult(bluetoothDeviceInfoDataList);
    }

    public synchronized boolean isContain(String address,int rssi,byte[] scanRecord)
    {
        for(BluetoothDeviceInfoData b : bluetoothDeviceInfoDataList)
        {
            if(address.equals(b.getBluetoothDevice().getAddress()))
            {
                b.setRssi(rssi);
                return true;
            }
        }
        return false;
    }

    /*
        ================================================
        bluetooth connect func
        ================================================
    */

    public void connectDevice(BluetoothDevice bluetoothDevice)
    {
        connect_bluetoothDevice = bluetoothDevice;
        connectAddress = bluetoothDevice.getAddress();
        if(is_connect)
        {
            if(bluetoothGatt != null)
            {
                bluetoothGatt.disconnect();
                return;
            }
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    bluetoothGatt = connect_bluetoothDevice.connectGatt(context, false, BluetoothManager.this, BluetoothDevice.TRANSPORT_LE);
                } else {
                    bluetoothGatt = connect_bluetoothDevice.connectGatt(context, false, BluetoothManager.this);
                }
            }
        });
    }


    public void disconnectDevice()
    {
        connectAddress = "";
        if(btAdapter != null)
        {
            if(bluetoothGatt != null)
            {
                bluetoothGatt.disconnect();
                bluetoothGatt = null;
                scanDevice(true);
            }
        }
    }

    public void closeDevice()
    {
        connectAddress = "";
        if(btAdapter != null)
        {
            if(bluetoothGatt != null)
            {
                is_connect = false;
                bluetoothGatt.close();
                bluetoothGatt = null;
                connectionCallback.onDisconnected(connect_bluetoothDevice);
                scanDevice(true);
            }
        }
    }

    /*
        ================================================
        bluetooth gatt default callback func
        ================================================
    */

    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if(newState == android.bluetooth.BluetoothProfile.STATE_CONNECTED)
        {
            scanDevice(false);
            connectionCallback.onConnected();
            bluetoothGatt = gatt;
            connectAddress = gatt.getDevice().getAddress();
            is_connect = true;
            Log.e("state","connected");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    gatt.discoverServices();
                }
            }).start();
        }
        else if(newState == android.bluetooth.BluetoothProfile.STATE_DISCONNECTED)
        {
            gatt.close();
            Log.e("state","disconnected");
            is_connect = false;
            bluetoothGatt = null;
            connectAddress = "";
            connectionCallback.onDisconnected(connect_bluetoothDevice);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        if(status == 0)
        {
            Log.e("write desc","suc");

        }else
        {
            Log.e("write desc","fail");
        }
    }

    @Override
    public synchronized void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        final byte[] data = characteristic.getValue();
    }

    @Override
    public synchronized void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        final byte[] data = characteristic.getValue();
        Log.d("data", byteTohex(data));
        return;
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);

        if(status == BluetoothGatt.GATT_SUCCESS)
        {

        }
        else if(status == BluetoothGatt.GATT_FAILURE)
        {
            //Send Fail
        }
        else if(status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED)
        {
            //No Permission
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        try {
            if (status == GATT_SUCCESS) {
                //Get service
                List<BluetoothGattService> services = gatt.getServices();
                for(BluetoothGattService bluetoothGattService :services)
                {
                    Log.e("service",bluetoothGattService.getUuid().toString());
                    for(BluetoothGattCharacteristic characteristic : bluetoothGattService.getCharacteristics())
                    {
                        Log.e("characteristic",characteristic.getUuid().toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        ================================================
        parser ble peripheral broadcast data
        ================================================
    */

    private byte[] specific_data = null;
    private String localName = "";
    private int txPowerLevel = 0;
    private static final int DATA_TYPE_FLAGS = 0x01;
    private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 0x02;
    private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 0x03;
    private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 0x04;
    private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 0x05;
    private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 0x06;
    private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 0x07;
    private static final int DATA_TYPE_LOCAL_NAME_SHORT = 0x08;
    private static final int DATA_TYPE_LOCAL_NAME_COMPLETE = 0x09;
    private static final int DATA_TYPE_TX_POWER_LEVEL = 0x0A;
    private static final int DATA_TYPE_SERVICE_DATA = 0x16;
    private static final int DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF;

    public void parseScanRecord(byte[] scanRecord)
    {
        txPowerLevel = -1;
        localName = "";
        int currentPos = 0;
        specific_data = null;

        if(scanRecord == null)
        {
            return;
        }

        while(currentPos < scanRecord.length)
        {
            int length = scanRecord[currentPos++] & 0xFF;
            if (length == 0) {
                break;
            }
            int dataLength = length - 1;
            int fieldType = scanRecord[currentPos++] & 0xFF;

            switch (fieldType) {
                case DATA_TYPE_FLAGS:
                    break;
                case DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL:
                case DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE:
                    break;
                case DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL:
                case DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE:
                    break;
                case DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL:
                case DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE:
                    break;
                case DATA_TYPE_LOCAL_NAME_SHORT:
                case DATA_TYPE_LOCAL_NAME_COMPLETE:
                    localName = new String(extractBytes(scanRecord, currentPos, dataLength));
                    break;
                case DATA_TYPE_TX_POWER_LEVEL:
                    txPowerLevel = scanRecord[currentPos];
                    break;
                case DATA_TYPE_SERVICE_DATA:
                    break;
                case DATA_TYPE_MANUFACTURER_SPECIFIC_DATA:
                    //broadcast device info.
                    specific_data = extractBytes(scanRecord, currentPos + 2,
                            dataLength - 2);
                    break;
                default:
                    break;
            }
            currentPos += dataLength;
        }
    }

    /*
        ================================================
        write to peripheral with byte array
        ================================================
    */

    public void sendData(byte[] data) {
        if(data != null)
        {
            if(bluetoothGatt != null)
            {
                BluetoothGattService service = bluetoothGatt.getService(bluetoothConfig.getWrite_service());
                if(service != null)
                {
                    final BluetoothGattCharacteristic characteristic = service.getCharacteristic(bluetoothConfig.getWrite_characteristic());
                    if(characteristic != null)
                    {
                            characteristic.setValue(data);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    bluetoothGatt.writeCharacteristic(characteristic);
                                }
                            });
                            return;
                    }
                }
            }
        }
        // connection error, close connect.
        closeDevice();
    }

    /*
        ================================================
        write to peripheral with byte array
        ================================================
    */

    // characteristic notify enable
    public void enableNotify() {
        try {
            BluetoothGattService service = bluetoothGatt.getService(bluetoothConfig.getService());
            if(service != null)
            {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(bluetoothConfig.getCharacteristic());
                if(characteristic != null)
                {
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                            bluetoothConfig.getDescriptor());
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bluetoothGatt.writeDescriptor(descriptor);
                    bluetoothGatt.setCharacteristicNotification(characteristic,true);
                    return;
                }
            }
            // connection error, close connect.
            closeDevice();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        ================================================
        Callback Interface
        ================================================
    */

    public interface BluetoothInterface
    {
        interface ScanDeviceCallBack
        {
            void ScanResult(List<BluetoothDeviceInfoData> bluetoothDeviceInfoDatas);
        }
        interface ConnectionCallback
        {
            void onConnected();
            void onDisconnected(BluetoothDevice bluetoothDevice);
        }
    }
}
