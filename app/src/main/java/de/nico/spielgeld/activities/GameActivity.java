package de.nico.spielgeld.activities;

import android.bluetooth.BluetoothDevice;

public abstract class GameActivity extends MainActivity {
    public abstract void sendMoney(BluetoothDevice device, Double amount);
}
