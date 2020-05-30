package de.nico.spielgeld.activities;

import android.bluetooth.BluetoothDevice;

public abstract class GameActivity extends MainActivity {
    public abstract void sendMoney(BluetoothDevice device, Double amount);
    String getBluetoothAddress() {
        //return Settings.Secure.getString(this.getContentResolver(), "bluetooth_address"); This does not work without a system level permission called LOCAL_MAC_ADDRESS
        return getBluetoothAdapter().getAddress(); // always returns 02:00:00:00:00:00
    }
}
