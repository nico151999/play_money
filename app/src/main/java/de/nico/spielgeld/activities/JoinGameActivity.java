package de.nico.spielgeld.activities;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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
    private ArrayList<BluetoothDevice> mFoundDevices;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        mDeviceListView = findViewById(R.id.found_devices);
        mSwipeRefreshLayout = findViewById(R.id.refresh_layout);
        mWaitInformationView = findViewById(R.id.wait_information);

        mFoundDevices = new ArrayList<>();

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
                    mWaitInformationView.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onFailedToEstablishConnection() {
                runOnUiThread(() -> {
                    Toast.makeText(JoinGameActivity.this, R.string.failed_connection, Toast.LENGTH_LONG).show();
                    mDeviceListView.setVisibility(View.VISIBLE);
                });
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
        if (mFoundDevices.isEmpty()) {
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            promptForUuidFetch(mFoundDevices.remove(0));
        }
    }

    @Override
    protected void onUuidFetched(BluetoothDevice device, Parcelable[] uuids) {
        if (uuids != null) {
            for (Parcelable uuid : uuids) {
                if (uuid.toString().equals(Constants.UUID.toString())) {
                    mJoinRecyclerAdapter.add(device);
                    break;
                }
            }
        }
        if (mFoundDevices.isEmpty()) {
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            promptForUuidFetch(mFoundDevices.remove(0));
        }
    }

    public void connectToServer(BluetoothDevice server) {
        mDeviceListView.setVisibility(View.GONE);
        mJoinGameService.connectToServer(server);
    }
}
