package de.nico.spielgeld.activities;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Pair;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedHashMap;

import de.nico.spielgeld.R;
import de.nico.spielgeld.views.ClientBluetoothDeviceRecyclerAdapter;

public abstract class GameActivity extends MainActivity {

    private ClientBluetoothDeviceRecyclerAdapter mOpponentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    public abstract void sendMoney(BluetoothDevice device, Double amount);
    String getBluetoothAddress() {
        //return Settings.Secure.getString(this.getContentResolver(), "bluetooth_address"); This does not work without a system level permission called LOCAL_MAC_ADDRESS
        return getBluetoothAdapter().getAddress(); // always returns 02:00:00:00:00:00
    }
    void setOpponentsAdapter(LinkedHashMap<BluetoothDevice, Pair<String, Double>> accounts) {
        RecyclerView opponentListView = findViewById(R.id.client_list);
        mOpponentsAdapter = new ClientBluetoothDeviceRecyclerAdapter(this, accounts);
        opponentListView.setAdapter(mOpponentsAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        opponentListView.setLayoutManager(layoutManager);
        opponentListView.addItemDecoration(new DividerItemDecoration(opponentListView.getContext(), layoutManager.getOrientation()));
    }

    ClientBluetoothDeviceRecyclerAdapter getOpponentsAdapter() {
        return mOpponentsAdapter;
    }
}
