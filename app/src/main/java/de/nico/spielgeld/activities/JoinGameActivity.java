package de.nico.spielgeld.activities;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

import de.nico.spielgeld.Constants;
import de.nico.spielgeld.InterActivitySockets;
import de.nico.spielgeld.R;
import de.nico.spielgeld.services.JoinGameService;
import de.nico.spielgeld.views.JoinBluetoothDeviceRecyclerAdapter;

public class JoinGameActivity extends MainActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mDeviceListView;
    private MaterialTextView mWaitInformationView;
    private JoinBluetoothDeviceRecyclerAdapter mJoinRecyclerAdapter;
    private JoinGameService mJoinGameService;

    private ArrayList<BluetoothDevice> mFoundDevices = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        mDeviceListView = findViewById(R.id.found_devices);
        mSwipeRefreshLayout = findViewById(R.id.refresh_layout);
        mWaitInformationView = findViewById(R.id.wait_information);

        mJoinRecyclerAdapter = new JoinBluetoothDeviceRecyclerAdapter(this);
        mDeviceListView.setAdapter(mJoinRecyclerAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mDeviceListView.setLayoutManager(layoutManager);
        mDeviceListView.addItemDecoration(new DividerItemDecoration(mDeviceListView.getContext(), layoutManager.getOrientation()));

        mJoinGameService = new JoinGameService(new JoinGameService.JoinGameServiceListener() {
            @Override
            public void onConnectedToServer() {
                runOnUiThread(() -> {
                    mSwipeRefreshLayout.setEnabled(false);
                    mJoinRecyclerAdapter.clear();
                    mDeviceListView.setVisibility(View.GONE);
                    mWaitInformationView.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onFailedToEstablishConnection() {
                runOnUiThread(() -> Toast.makeText(JoinGameActivity.this, R.string.failed_connection, Toast.LENGTH_LONG).show());
            }

            @Override
            public void onHostStartedGame(BluetoothSocket socket) {
                InterActivitySockets.host = socket;
                startActivity(new Intent(JoinGameActivity.this, GameClientActivity.class));
                finish();
            }

            @Override
            public void onUnexpectedMessageReceived() {
                runOnUiThread(() -> Toast.makeText(JoinGameActivity.this, R.string.error_occurred, Toast.LENGTH_LONG).show());
                finish();
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (!getBluetoothAdapter().isDiscovering()) {
                getBluetoothAdapter().startDiscovery();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!getBluetoothAdapter().isDiscovering() && mSwipeRefreshLayout.isEnabled()) {
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
            getBluetoothAdapter().startDiscovery();
        }
    }

    @Override
    protected void onBluetoothDiscoveryDeviceFound(BluetoothDevice device) {
        mFoundDevices.add(device);
    }

    @Override
    protected void onBluetoothDiscoveryFinished() {
        for (BluetoothDevice device : mFoundDevices) {
            ParcelUuid[] uuids = device.getUuids();
            if (uuids != null) {
                for (ParcelUuid uuid : uuids) {
                    if (uuid.getUuid().compareTo(Constants.UUID) == 0) {
                        mJoinRecyclerAdapter.add(device);
                        break;
                    }
                }
            }
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void connectToServer(BluetoothDevice server) {
        mJoinGameService.connectToServer(server);
    }
}
