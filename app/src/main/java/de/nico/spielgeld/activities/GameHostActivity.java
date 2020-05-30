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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.nico.spielgeld.Constants;
import de.nico.spielgeld.InterActivitySockets;
import de.nico.spielgeld.R;
import de.nico.spielgeld.parser.RemoveMessage;
import de.nico.spielgeld.parser.RequestMessage;
import de.nico.spielgeld.parser.SendMessage;
import de.nico.spielgeld.parser.StandingMessage;
import de.nico.spielgeld.parser.StartMessage;
import de.nico.spielgeld.parser.UpdateMessage;
import de.nico.spielgeld.services.GameHostService;
import de.nico.spielgeld.views.ClientBluetoothDeviceRecyclerAdapter;

public class GameHostActivity extends GameActivity {

    private GameHostService mGameHostService;
    private ClientBluetoothDeviceRecyclerAdapter mClientAdapter;
    private MenuItem mAccountItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_host_client);

        RecyclerView clientListView = findViewById(R.id.client_list);

        List<BluetoothSocket> clientSockets = InterActivitySockets.clients;
        if (clientSockets == null) {
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_LONG).show();
            finish();
            return;
        } else {
            InterActivitySockets.clients = null;
        }
        LinkedHashMap<BluetoothDevice, Double> accounts = new LinkedHashMap<>();
        for (BluetoothSocket socket : clientSockets) {
            accounts.put(socket.getRemoteDevice(), Constants.INITIAL_ACCOUNT);
        }

        mClientAdapter = new ClientBluetoothDeviceRecyclerAdapter(this, accounts);
        clientListView.setAdapter(mClientAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        clientListView.setLayoutManager(layoutManager);
        clientListView.addItemDecoration(new DividerItemDecoration(clientListView.getContext(), layoutManager.getOrientation()));

        mGameHostService = new GameHostService(new GameHostService.GameHostListener() {
            @Override
            public void onReaderCreationError(BluetoothSocket client) {
                runOnUiThread(() -> Toast.makeText(GameHostActivity.this, getString(R.string.cannot_read, client.getRemoteDevice().getName()), Toast.LENGTH_LONG).show());
                finish();
            }

            @Override
            public void onWriterCreationError(BluetoothSocket client) {
                runOnUiThread(() -> Toast.makeText(GameHostActivity.this, getString(R.string.cannot_write, client.getRemoteDevice().getName()), Toast.LENGTH_LONG).show());
                finish();
            }

            @Override
            public void onMessageReceived(BluetoothSocket client, String message) {
                Object messageObject;
                if ((messageObject = SendMessage.parse(message)) != null) {
                    receiveSendMessage(client.getRemoteDevice(), (SendMessage) messageObject);
                } else if ((messageObject = RequestMessage.parse(message)) != null) {
                    receiveRequestMessage(client.getRemoteDevice());
                } else {
                    runOnUiThread(() -> Toast.makeText(GameHostActivity.this, R.string.unknown_message_format, Toast.LENGTH_LONG).show());
                }
            }

            @Override
            public void onConnectionInterrupted(BluetoothSocket client) {
                runOnUiThread(() -> Toast.makeText(GameHostActivity.this, getString(R.string.connection_interrupted, client.getRemoteDevice().getName()), Toast.LENGTH_LONG).show());
                BluetoothDevice device = client.getRemoteDevice();
                mGameHostService.close(device);
                runOnUiThread(() -> mClientAdapter.remove(device));
                mGameHostService.runOnAll(bluetoothDevice -> mGameHostService.write(bluetoothDevice, RemoveMessage.create(device.getAddress()).toString()));
            }
        }, clientSockets);

        mGameHostService.runOnAll(device -> {
            if (!mGameHostService.write(device, StartMessage.create().toString())) {
                Toast.makeText(this, getString(R.string.device_cannot_start, device.getName()), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGameHostService.runOnAll(device -> mGameHostService.close(device));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account, menu);
        mAccountItem = menu.findItem(R.id.account);
        if (mAccountItem.getTitle().length() == 0) {
            mAccountItem.setTitle(Constants.INITIAL_ACCOUNT.toString());
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void sendMoney(BluetoothDevice receivingDevice, Double amount) {
        Double newOwnAmount = Double.parseDouble(mAccountItem.getTitle().toString()) - amount;
        mAccountItem.setTitle(newOwnAmount.toString());
        Double newReceiverAmount = mClientAdapter.update(receivingDevice, amount);
        String updateReceiverMessage = UpdateMessage.create(newReceiverAmount, receivingDevice.getAddress()).toString();
        String updateSenderMessage = UpdateMessage.create(newOwnAmount, getBluetoothAddress()).toString();
        mGameHostService.runOnAll(device -> {
            mGameHostService.write(device, updateReceiverMessage);
            mGameHostService.write(device, updateSenderMessage);
        });
    }

    private void receiveSendMessage(BluetoothDevice sendingDevice, SendMessage sendMessage) {
        runOnUiThread(() -> {
            BluetoothDevice receivingDevice = getBluetoothAdapter().getRemoteDevice(sendMessage.getTargetAddress());
            Double newValueReceiver;
            if (receivingDevice.getAddress().equals(getBluetoothAddress())) {
                newValueReceiver = Double.parseDouble(mAccountItem.getTitle().toString()) + sendMessage.getAmount();
                mAccountItem.setTitle(newValueReceiver.toString());
            } else {
                newValueReceiver = mClientAdapter.update(receivingDevice, sendMessage.getAmount());
            }
            Double newValueSender = mClientAdapter.update(sendingDevice, -sendMessage.getAmount());
            String updateReceiverMessage = UpdateMessage.create(newValueReceiver, receivingDevice.getAddress()).toString();
            String updateSenderMessage = UpdateMessage.create(newValueSender, sendingDevice.getAddress()).toString();
            mGameHostService.runOnAll(device -> {
                mGameHostService.write(device, updateReceiverMessage);
                mGameHostService.write(device, updateSenderMessage);
            });
        });
    }

    private void receiveRequestMessage(BluetoothDevice requestingDevice) {
        Map<String, Double> standings = new HashMap<>();
        for (Map.Entry<BluetoothDevice, Double> account : mClientAdapter.getAccounts().entrySet()) {
            standings.put(account.getKey().getAddress(), account.getValue());
        }
        standings.put(getBluetoothAddress(), Double.parseDouble(mAccountItem.getTitle().toString()));
        mGameHostService.write(requestingDevice, StandingMessage.create(standings, requestingDevice.getAddress()).toString());
    }
}