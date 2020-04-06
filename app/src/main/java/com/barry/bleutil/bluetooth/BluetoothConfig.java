package com.barry.bleutil.bluetooth;

import java.util.UUID;

public class BluetoothConfig {

    private UUID service = UUID.fromString("");
    private UUID characteristic = UUID.fromString("");
    private UUID descriptor = UUID.fromString("");
    private UUID write_service = UUID.fromString("");
    private UUID write_characteristic = UUID.fromString("");
    private UUID write_descriptor = UUID.fromString("");
    public BluetoothConfig() {
    }

    public UUID getService() {
        return service;
    }

    public UUID getCharacteristic() {
        return characteristic;
    }

    public UUID getDescriptor() {
        return descriptor;
    }

    public void setService(UUID service) {
        this.service = service;
    }

    public void setCharacteristic(UUID characteristic) {
        this.characteristic = characteristic;
    }

    public void setDescriptor(UUID descriptor) {
        this.descriptor = descriptor;
    }

    public UUID getWrite_service() {
        return write_service;
    }

    public UUID getWrite_characteristic() {
        return write_characteristic;
    }

    public UUID getWrite_descriptor() {
        return write_descriptor;
    }

    public void setWrite_service(UUID write_service) {
        this.write_service = write_service;
    }

    public void setWrite_characteristic(UUID write_characteristic) {
        this.write_characteristic = write_characteristic;
    }

    public void setWrite_descriptor(UUID write_descriptor) {
        this.write_descriptor = write_descriptor;
    }
}
