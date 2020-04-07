# BluetoothManager Tool

## Description
BluetoothManager 作為 App 與 Bluetooth Low Energy 裝置溝通橋梁，使其運用Android裝置藍芽功能進行掃描,資料溝通等行為

## Permission

    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

## Function
+ 初始 Bluetooth Manager
1. 傳入applicationcontext
2. 傳入掃描結果及連線狀態 CallBack
```
BluetoothManager bluetoothManager = new BluetoothManager(getApplicationContext(),scanDeviceCallBack,readDataCallBack);
```
+ 掃描裝置
```
bluetoothManager.scanDevice(true);
```
```
bluetoothManager.scanDevice(false);
```


+ 連線裝置
1. 進行連線後,需從"onConnectionStateChange" callback得知連線以及斷開連線時的及時狀態
```
bluetoothManager.connectDevice(BluetoothDevice)
```
```
bluetoothManager.disconnectDevice();
```

+ Bluetooth Low Energy資料傳輸
1. 資料輸入(需預先設置 Service 及 Characteristic 資訊於 BluetoothConfig,否則須另外處理)
2. 內部傳輸結果可從 "onCharacteristicWrite" callback得知成功與否
3. 批量資料輸入的情況,需等待前次結果完成後再往下輸入

```
byte[] rawdata = "Hello World".getBytes();
bluetoothManager.sendData(rawdata);
```
1. 資料讀取,可從 "onCharacteristicRead" 及 "onCharacteristicChanged" 取得bluetooth peripheral 回傳之資料
2. 第一次資訊將從 "onCharacteristicRead" 取得,爾後資訊將從"onCharacteristicChanged" 取得


## issue
1. 如果掃描時長超過一定秒數,根據不同裝置,掃描行為將被隱蔽,需重新關閉掃描再開啟才能正常運作.
2. android 及 ios 所能取得連線方式不同, android由mac address連線操作, ios 則由service uuid進行連線操作
