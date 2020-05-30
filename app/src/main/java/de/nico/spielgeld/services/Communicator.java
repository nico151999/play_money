package de.nico.spielgeld.services;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.nico.spielgeld.Constants;

class Communicator extends Thread {

    private final BluetoothSocket mmHost;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final CommunicatorListener mCommunicatorListener;

    Communicator(BluetoothSocket host, CommunicatorListener communicatorListener) {
        mmHost = host;
        mCommunicatorListener = communicatorListener;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = host.getInputStream();
        } catch (IOException e) {
            this.mCommunicatorListener.onReaderCreationError(host);
        }
        try {
            tmpOut = host.getOutputStream();
        } catch (IOException e) {
            this.mCommunicatorListener.onWriterCreationError(host);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    @Override
    public void run() {
        List<Byte> bytes = new ArrayList<>();
        byte buffer;
        int ret;
        while (!isInterrupted()) {
            try {
                ret = mmInStream.read();
                if (ret == -1) {
                    mCommunicatorListener.onConnectionInterrupted(mmHost);
                    break;
                }
                buffer = (byte) ret;
                if (buffer == Constants.TERMINATING_BYTE) {
                    byte[] transferredBytes = new byte[bytes.size()];
                    for (int i = 0; i < bytes.size(); i++) {
                        transferredBytes[i] = bytes.get(i);
                    }
                    mCommunicatorListener.onMessageReceived(mmHost, new String(transferredBytes));
                } else {
                    bytes.add(buffer);
                }
            } catch (IOException e) {
                if (!isInterrupted()) {
                    mCommunicatorListener.onConnectionInterrupted(mmHost);
                }
            }
        }
    }

    boolean write(String message) {
        try {
            byte[] bytes = message.getBytes();
            byte[] byteMessage = new byte[bytes.length + 1];
            System.arraycopy(bytes, 0, byteMessage, 0, bytes.length);
            byteMessage[bytes.length] = Constants.TERMINATING_BYTE;
            mmOutStream.write(byteMessage);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    void close() {
        if (!interrupted()) {
            interrupt();
        }
        try {
            mmHost.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    interface CommunicatorListener {
        void onReaderCreationError(BluetoothSocket client);
        void onWriterCreationError(BluetoothSocket client);
        void onMessageReceived(BluetoothSocket client, String message);
        void onConnectionInterrupted(BluetoothSocket client);
    }
}