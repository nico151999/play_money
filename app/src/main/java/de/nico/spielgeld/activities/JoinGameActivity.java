package de.nico.spielgeld.activities;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.transition.Fade;
import android.transition.Transition;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import de.nico.spielgeld.Constants;
import de.nico.spielgeld.InterActivitySockets;
import de.nico.spielgeld.R;
import de.nico.spielgeld.services.JoinGameService;
import de.nico.spielgeld.views.JoinBluetoothDeviceRecyclerAdapter;

public class JoinGameActivity extends MainActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MaterialCheckBox mShowAllView;
    private LinearLayout mDevicesContainerView;
    private MaterialTextView mWaitInformationView;
    private JoinBluetoothDeviceRecyclerAdapter mJoinRecyclerAdapter;
    private JoinGameService mJoinGameService;
    private int mFetchedDeviceCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_join_game);

        Transition fade = new Fade();
        fade.excludeTarget(R.id.appbar_layout, true);
        fade.excludeTarget(android.R.id.navigationBarBackground,true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        getWindow().setExitTransition(fade);
        getWindow().setEnterTransition(fade);

        RecyclerView deviceListView = findViewById(R.id.found_devices);
        mSwipeRefreshLayout = findViewById(R.id.refresh_layout);
        mWaitInformationView = findViewById(R.id.wait_information);
        mShowAllView = findViewById(R.id.show_all);
        mDevicesContainerView = findViewById(R.id.found_devices_container);

        mJoinRecyclerAdapter = new JoinBluetoothDeviceRecyclerAdapter(this);
        deviceListView.setAdapter(mJoinRecyclerAdapter);
        deviceListView.setLayoutManager(new LinearLayoutManager(this));

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
                    mDevicesContainerView.setVisibility(View.VISIBLE);
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

        mShowAllView.setOnCheckedChangeListener((view, checked) -> mJoinRecyclerAdapter.notifyDataSetChanged());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mSwipeRefreshLayout.isRefreshing() && mDevicesContainerView.getVisibility() == View.VISIBLE) {
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
            getBluetoothAdapter().startDiscovery();
        }
    }

    @Override
    protected void onBluetoothDiscoveryDeviceFound(BluetoothDevice device) {
        mJoinRecyclerAdapter.add(device, false);
    }

    @Override
    protected void onBluetoothDiscoveryFinished() {
        mFetchedDeviceCount = mJoinRecyclerAdapter.getItemCount();
        uuidFetch();
    }

    @Override
    protected void onUuidFetched(BluetoothDevice device, Parcelable[] uuids) {
        if (uuids != null) {
            for (Parcelable uuid : uuids) {
                if (uuid.toString().equals(Constants.UUID.toString())) {
                    mJoinRecyclerAdapter.setIsAppDevice(device);
                    break;
                }
            }
        }
        uuidFetch();
    }

    public boolean isShowAllEnabled() {
        return mShowAllView.isChecked();
    }

    private void uuidFetch() {
        if (mFetchedDeviceCount-- > 0 && mDevicesContainerView.getVisibility() == View.VISIBLE) {
            promptForUuidFetch(mJoinRecyclerAdapter.getBluetoothDevice(mFetchedDeviceCount));
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void connectToServer(BluetoothDevice server) {
        if (getBluetoothAdapter().isDiscovering()) {
            getBluetoothAdapter().cancelDiscovery();
            mSwipeRefreshLayout.setRefreshing(false);
        }
        mDevicesContainerView.setVisibility(View.GONE);
        mJoinGameService.connectToServer(server);
    }
}
