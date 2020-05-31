package de.nico.spielgeld.activities;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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

public class GameHostActivity extends GameActivity {

    private GameHostService mGameHostService;
    private MenuItem mAccountItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<BluetoothSocket> clientSockets = InterActivitySockets.clients;
        if (clientSockets == null) {
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_LONG).show();
            finish();
            return;
        } else {
            InterActivitySockets.clients = null;
        }
        LinkedHashMap<BluetoothDevice, Pair<String, Double>> accounts = new LinkedHashMap<>();
        for (BluetoothSocket socket : clientSockets) {
            accounts.put(socket.getRemoteDevice(), new Pair<>(socket.getRemoteDevice().getName(), Constants.INITIAL_ACCOUNT));
        }
        setOpponentsAdapter(accounts);

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
                runOnUiThread(() -> getOpponentsAdapter().remove(device));
                mGameHostService.runOnAll(bluetoothDevice -> mGameHostService.write(bluetoothDevice, new RemoveMessage(device.getAddress()).toString()));
            }
        }, clientSockets);
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
            mGameHostService.runOnAll(device -> {
                if (!mGameHostService.write(device, new StartMessage().toString())) {
                    Toast.makeText(this, getString(R.string.device_cannot_start, device.getName()), Toast.LENGTH_LONG).show();
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void sendMoney(BluetoothDevice receivingDevice, Double amount) {
        Double newOwnAmount = Double.parseDouble(mAccountItem.getTitle().toString()) - amount;
        mAccountItem.setTitle(newOwnAmount.toString());
        Double newReceiverAmount = getOpponentsAdapter().update(receivingDevice, amount);
        String updateReceiverMessage = new UpdateMessage(newReceiverAmount, receivingDevice.getAddress()).toString();
        String updateSenderMessage = new UpdateMessage(newOwnAmount, getBluetoothAddress()).toString();
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
                newValueReceiver = getOpponentsAdapter().update(receivingDevice, sendMessage.getAmount());
            }
            Double newValueSender = getOpponentsAdapter().update(sendingDevice, -sendMessage.getAmount());
            String updateReceiverMessage = new UpdateMessage(newValueReceiver, receivingDevice.getAddress()).toString();
            String updateSenderMessage = new UpdateMessage(newValueSender, sendingDevice.getAddress()).toString();
            mGameHostService.runOnAll(device -> {
                mGameHostService.write(device, updateReceiverMessage);
                mGameHostService.write(device, updateSenderMessage);
            });
        });
    }

    private void receiveRequestMessage(BluetoothDevice requestingDevice) {
        Map<String, Pair<String, Double>> standings = new HashMap<>();
        for (Map.Entry<BluetoothDevice, Pair<String, Double>> account : getOpponentsAdapter().getAccounts().entrySet()) {
            standings.put(account.getKey().getAddress(), account.getValue());
        }
        standings.put(getBluetoothAddress(), new Pair<>(getBluetoothAdapter().getName(), Double.parseDouble(mAccountItem.getTitle().toString())));
        mGameHostService.write(requestingDevice, new StandingMessage(standings, requestingDevice.getAddress()).toString());
    }
}