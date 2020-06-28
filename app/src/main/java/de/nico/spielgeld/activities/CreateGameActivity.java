package de.nico.spielgeld.activities;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import de.nico.spielgeld.InterActivitySockets;
import de.nico.spielgeld.R;
import de.nico.spielgeld.services.CreateGameService;
import de.nico.spielgeld.views.JointBluetoothDeviceRecyclerAdapter;

public class CreateGameActivity extends MainActivity {

    private CreateGameService mCreateGameService;
    private JointBluetoothDeviceRecyclerAdapter mJointRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_create_game);

        MaterialButton startGameView = findViewById(R.id.start_game);
        RecyclerView recyclerView = findViewById(R.id.joint_devices);

        mJointRecyclerAdapter = new JointBluetoothDeviceRecyclerAdapter(this);
        recyclerView.setAdapter(mJointRecyclerAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation()));

        mCreateGameService = new CreateGameService(getBluetoothAdapter(), new CreateGameService.CreateGameListener() {
            @Override
            public void onFailToCreateServer() {
                runOnUiThread(() -> Toast.makeText(CreateGameActivity.this, R.string.cannot_host, Toast.LENGTH_LONG).show());
                finish();
            }

            @Override
            public void onFailToCreateIncomingConnection() {
                runOnUiThread(() -> Toast.makeText(CreateGameActivity.this, R.string.cannot_connect_incoming, Toast.LENGTH_LONG).show());
            }

            @Override
            public void onClientConnected(BluetoothSocket client) {
                runOnUiThread(() -> mJointRecyclerAdapter.add(client));
            }
        });
        mCreateGameService.start();

        startGameView.setOnClickListener((view) -> {
            if (mCreateGameService != null) {
                mCreateGameService.closeServer();
                InterActivitySockets.clients = mJointRecyclerAdapter.getClients();
                startActivity(new Intent(CreateGameActivity.this, GameHostActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        promptForDiscoverability();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCreateGameService != null) {
            mCreateGameService.closeServer();
        }
    }

    @Override
    protected void onDiscoverabilityPromptResult(boolean discoverable) {
        if (!discoverable) {
            Toast.makeText(CreateGameActivity.this, R.string.host_requires_discoverability, Toast.LENGTH_LONG).show();
            finish();
        }
    }
}