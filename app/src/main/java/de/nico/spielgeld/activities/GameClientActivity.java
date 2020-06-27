package de.nico.spielgeld.activities;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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

public class GameClientActivity extends GameActivity {
    private GameClientService mGameClientService;
    private String mBluetoothAddress; // needs to be set by server, as own address cannot be determined
    private MenuItem mAccountItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                System.out.println("The host finished the game");
                finish();
            }

            @Override
            public void onMessageReceived(String message) {
                Object messageObject;
                if ((messageObject = StandingMessage.parse(message)) != null) {
                    receiveStandingMessage((StandingMessage) messageObject);
                } else if (mBluetoothAddress == null) {
                    System.out.println("Waiting for a StandingMessage which will set the address, before other messages can be processed");
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account, menu);
        mAccountItem = menu.findItem(R.id.account);
        if (!mGameClientService.write(new RequestMessage().toString())) {
            Toast.makeText(this, R.string.cannot_initialize_game, Toast.LENGTH_LONG).show();
            finish();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGameClientService.close();
    }

    private void receiveUpdateMessage(UpdateMessage updateMessage) {
        runOnUiThread(() -> {
            if (updateMessage.getDeviceAddress().equals(mBluetoothAddress)) {
                mAccountItem.setTitle(updateMessage.getAccount().toString());
            } else {
                getOpponentsAdapter().updateTotal(getBluetoothAdapter().getRemoteDevice(updateMessage.getDeviceAddress()), updateMessage.getAccount());
            }
        });
    }

    private void receiveRemoveMessage(RemoveMessage removeMessage) {
        runOnUiThread(() -> getOpponentsAdapter().remove(getBluetoothAdapter().getRemoteDevice(removeMessage.getDevice())));
    }

    private void receiveStandingMessage(StandingMessage standingMessage) {
        mBluetoothAddress = standingMessage.getBluetoothAddress();
        LinkedHashMap<BluetoothDevice, Pair<String, Integer>> accounts = new LinkedHashMap<>();
        for (Map.Entry<String, Pair<String, Integer>> account : standingMessage.getAccounts().entrySet()) {
            if (account.getKey().equals(mBluetoothAddress)) {
                runOnUiThread(() -> mAccountItem.setTitle(account.getValue().second.toString()));
            } else {
                accounts.put(getBluetoothAdapter().getRemoteDevice(account.getKey()), new Pair<>(account.getValue().first, account.getValue().second));
            }
        }
        runOnUiThread(() -> setOpponentsAdapter(accounts));
    }

    @Override
    public void sendMoney(BluetoothDevice device, Integer amount) {
        mGameClientService.write(new SendMessage(amount, device.getAddress()).toString());
    }
}
