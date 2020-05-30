package de.nico.spielgeld.services;

import android.bluetooth.BluetoothSocket;

public class GameClientService {

    private Communicator mCommunicator;

    public GameClientService(GameClientListener gameClientListener, BluetoothSocket host) {
        mCommunicator = new Communicator(host, new Communicator.CommunicatorListener() {
            @Override
            public void onReaderCreationError(BluetoothSocket client) {
                gameClientListener.onReaderCreationError();
            }

            @Override
            public void onWriterCreationError(BluetoothSocket client) {
                gameClientListener.onWriterCreationError();
            }

            @Override
            public void onMessageReceived(BluetoothSocket client, String message) {
                gameClientListener.onMessageReceived(message);
            }

            @Override
            public void onConnectionInterrupted(BluetoothSocket client) {
                gameClientListener.onConnectionInterrupted();
            }
        });
    }

    public boolean write(String message) {
        return mCommunicator.write(message);
    }

    public void close() {
        mCommunicator.close();
    }

    public interface GameClientListener {
        void onConnectionInterrupted();
        void onMessageReceived(String message);
        void onReaderCreationError();
        void onWriterCreationError();
    }
}
