package com.hdd.crane.logistics;

public class CourierBackup extends Courier {

    private static CourierBackup instance = new CourierBackup();

    private CourierBackup() {
    }

    public static CourierBackup getInstance() {
        return instance;
    }

}
