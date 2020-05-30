package de.nico.spielgeld.services;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.nico.spielgeld.Constants;
import de.nico.spielgeld.parser.StartMessage;

public class JoinGameService {

    private JoinGameServiceListener mJoinGameServiceListener;

    public JoinGameService(JoinGameServiceListener joinGameServiceListener) {
        mJoinGameServiceListener = joinGameServiceListener;
    }

    public void connectToServer(BluetoothDevice server) {
        new ConnectToServer(server).start();
    }

    private class ConnectToServer extends Thread {

        private BluetoothDevice mmServer;

        private ConnectToServer(BluetoothDevice server) {
            mmServer = server;
        }

        @Override
        public void run() {
            try {
                BluetoothSocket socket = mmServer.createRfcommSocketToServiceRecord(Constants.UUID);
                try {
                    socket.connect();
                    mJoinGameServiceListener.onConnectedToServer();
                    InputStream inStream = socket.getInputStream();
                    List<Byte> bytes = new ArrayList<>();
                    byte buffer;
                    int ret;
                    while (true) {
                        ret = inStream.read();
                        if (ret == -1) {
                            break;
                        }
                        buffer = (byte) ret;
                        if (buffer == Constants.TERMINATING_BYTE) {
                            break;
                        } else {
                            bytes.add(buffer);
                        }
                    }
                    byte[] transferredBytes = new byte[bytes.size()];
                    for (int i = 0; i < bytes.size(); i++) {
                        transferredBytes[i] = bytes.get(i);
                    }
                    if (StartMessage.parse(new String(transferredBytes)) == null) {
                        mJoinGameServiceListener.onUnexpectedMessageReceived();
                    } else {
                        mJoinGameServiceListener.onHostStartedGame(socket);
                    }
                } catch(IOException e) {
                    System.err.println("Could not connect to server: " + e.toString());
                    mJoinGameServiceListener.onFailedToEstablishConnection();
                    try {
                        socket.close();
                    } catch(IOException close) {
                        System.out.println("Could not close connection: " + close.toString());
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to create Socket: " + e.toString());
                mJoinGameServiceListener.onFailedToEstablishConnection();
            }
        }
    }

    public interface JoinGameServiceListener {
        void onConnectedToServer();
        void onFailedToEstablishConnection();
        void onHostStartedGame(BluetoothSocket socket);
        void onUnexpectedMessageReceived();
    }
}
