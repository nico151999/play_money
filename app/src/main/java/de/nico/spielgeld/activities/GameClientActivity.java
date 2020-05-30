package de.nico.spielgeld.activities;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedHashMap;
import java.util.Map;

import de.nico.spielgeld.InterActivitySockets;
import de.nico.spielgeld.R;
import de.nico.spielgeld.parser.RemoveMessage;
import de.nico.spielgeld.parser.RequestMessage;
import de.nico.spielgeld.parser.SendMessage;
import de.nico.spielgeld.parser.StandingMessage;
import de.nico.spielgeld.parser.UpdateMessage;
import de.nico.spielgeld.services.GameClientService;
import de.nico.spielgeld.views.ClientBluetoothDeviceRecyclerAdapter;

public class GameClientActivity extends GameActivity {

    private ClientBluetoothDeviceRecyclerAdapter mOpponentsAdapter;
    private GameClientService mGameClientService;
    private MenuItem mAccountItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_host_client);

        BluetoothSocket host = InterActivitySockets.host;
        if (host == null) {
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_LONG).show();
            finish();
            return;
        } else {
            InterActivitySockets.host = null;
        }
        mGameClientService = new GameClientService(new GameClientService.GameClientListener() {
            @Override
            public void onConnectionInterrupted() {
                runOnUiThread(() -> Toast.makeText(GameClientActivity.this, R.string.server_ended, Toast.LENGTH_LONG).show());
                finish();
            }

            @Override
            public void onMessageReceived(String message) {
                Object messageObject;
                if ((messageObject = StandingMessage.parse(message)) != null) {
                    receiveStandingMessage((StandingMessage) messageObject);
                } else if ((messageObject = RemoveMessage.parse(message)) != null) {
                    receiveRemoveMessage((RemoveMessage) messageObject);
                } else if ((messageObject = UpdateMessage.parse(message)) != null) {
                    receiveUpdateMessage((UpdateMessage) messageObject);
                } else {
                    runOnUiThread(() -> Toast.makeText(GameClientActivity.this, R.string.unknown_message_format, Toast.LENGTH_LONG).show());
                }
            }

            @Override
            public void onReaderCreationError() {
                runOnUiThread(() -> Toast.makeText(GameClientActivity.this, R.string.cannot_read_from_server, Toast.LENGTH_LONG).show());
                finish();
            }

            @Override
            public void onWriterCreationError() {
                runOnUiThread(() -> Toast.makeText(GameClientActivity.this, R.string.cannot_write_to_server, Toast.LENGTH_LONG).show());
                finish();
            }
        }, host);
        if (!mGameClientService.write(RequestMessage.create().toString())) {
            Toast.makeText(this, R.string.cannot_initialize_game, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account, menu);
        mAccountItem = menu.findItem(R.id.account);
        return super.onCreateOptionsMenu(menu);
    }

    private void receiveUpdateMessage(UpdateMessage updateMessage) {
        runOnUiThread(() -> {
            if (updateMessage.getDeviceAddress().equals(getBluetoothAdapter().getAddress())) {
                mAccountItem.setTitle(updateMessage.getAccount().toString());
            } else {
                mOpponentsAdapter.updateTotal(getBluetoothAdapter().getRemoteDevice(updateMessage.getDeviceAddress()), updateMessage.getAccount());
            }
        });
    }

    private void receiveRemoveMessage(RemoveMessage removeMessage) {
        runOnUiThread(() -> mOpponentsAdapter.remove(getBluetoothAdapter().getRemoteDevice(removeMessage.getDevice())));
    }

    private void receiveStandingMessage(StandingMessage standingMessage) {
        LinkedHashMap<BluetoothDevice, Double> accounts = new LinkedHashMap<>();
        for (Map.Entry<String, Double> account : standingMessage.getAccounts().entrySet()) {
            if (account.getKey().equals(getBluetoothAdapter().getAddress())) {
                runOnUiThread(() -> mAccountItem.setTitle(account.getValue().toString()));
            } else {
                accounts.put(getBluetoothAdapter().getRemoteDevice(account.getKey()), account.getValue());
            }
        }
        runOnUiThread(() -> {
            RecyclerView opponentListView = findViewById(R.id.client_list);
            mOpponentsAdapter = new ClientBluetoothDeviceRecyclerAdapter(this, accounts);
            opponentListView.setAdapter(mOpponentsAdapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            opponentListView.setLayoutManager(layoutManager);
            opponentListView.addItemDecoration(new DividerItemDecoration(opponentListView.getContext(), layoutManager.getOrientation()));
        });
    }

    @Override
    public void sendMoney(BluetoothDevice device, Double amount) {
        mGameClientService.write(SendMessage.create(amount, device.getAddress()).toString());
    }
}
