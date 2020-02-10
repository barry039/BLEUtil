# BLE Tool

## Description
The BLEUtil would help you to scan around peripheral, connect & communication with ble peripheral.

## Permission

    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

## Function
+ Scan Peripheral
```
BLEUtil bleUtil = new BLEUtil(getApplicationContext(),scanDeviceCallBack,readDataCallBack);
//start scan
bleUtil.scanDevice(true);
//stop scan
bleUtil.scanDevice(false);

//received peripheral
scanDeviceCallBack = new BLEUtil.BluetoothInterface.ScanDeviceCallBack() {
        @Override
        public void ScanResult(List<BluetoothDeviceInfoData> bluetoothDeviceInfoDatas) {
            //scaned list device
        }
    };
```
+ Connect Peripheral
```
BLEUtil bleUtil = new BLEUtil(getApplicationContext(),scanDeviceCallBack,readDataCallBack);

//connect peripheral, it would be a uncatch state until the ReadDataCallBack called  onConnected or onDisconnected
bleUtil.connectDevice(BluetoothDevice)

//disconnect peripheral,it would not shutdown the connection. It would be a uncatch state until the onConnectionStateChange called. The method parmeter new state equal android.bluetooth.BluetoothProfile.STATE_DISCONNECTED. This state could be re-connect or just close.
bleUtil.disconnectDevice();

//close peripheral, the connection would shutdown in time.
bleUtil.closeDevice();

//According to demand and behavior, it's could be more state in here.
readDataCallBack = new BLEUtil.BluetoothInterface.ReadDataCallBack() {
        @Override
        public void onConnected() {
            //called when peripheral connected.
        }

        @Override
        public void onDisconnected(BluetoothDevice bluetoothDevice) {
            //called when peripheral disconnect or close
        }
    };
```
+ Communication With BLE Peripheral
```
//after connect completed.
//send raw data command to peripheral
byte[] rawdata = "Hello World".getBytes();
bleUtil.sendData(rawdata);

//BLEUtil method:onCharacteristicWrite would be called when result is suc or fail.
//If situation is multiple command, it need wait the onCharacteristicWrite() called then send next, otherwise action would always fail.

//raw response from BLEUtil::onCharacteristicRead() & BLEUtil::onCharacteristicChanged()

final byte[] data = characteristic.getValue();
Log.d("data", byteTohex(data));

```


