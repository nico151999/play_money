package de.nico.spielgeld.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

import de.nico.spielgeld.Constants;

public class CreateGameService extends Thread {
    private BluetoothAdapter mBTAdapter;
    private CreateGameListener mCreateGameListener;
    private BluetoothServerSocket mServerSocket;


    public CreateGameService(BluetoothAdapter BTAdapter, CreateGameListener createGameListener) {
        mBTAdapter = BTAdapter;
        mCreateGameListener = createGameListener;
    }

    @Override
    public void run() {
        try {
            mServerSocket = mBTAdapter.listenUsingRfcommWithServiceRecord(Constants.GAME_NAME, Constants.UUID);
        } catch(IOException e) {
            mCreateGameListener.onFailToCreateServer();
            return;
        }
        while(!isInterrupted()) {
            try {
                mCreateGameListener.onClientConnected(mServerSocket.accept());
            } catch (IOException e) {
                if (!isInterrupted()) {
                    mCreateGameListener.onFailToCreateIncomingConnection();
                }
                break;
            }
        }
    }

    public void closeServer() {
        interrupt();
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                System.out.println("Could not close connection: " + e.toString());
            }
        }
    }

    public interface CreateGameListener {
        void onFailToCreateServer();
        void onFailToCreateIncomingConnection();
        void onClientConnected(BluetoothSocket client);
    }
}