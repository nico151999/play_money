package de.nico.spielgeld.services;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import androidx.core.util.Consumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameHostService {

    private Map<BluetoothDevice, Communicator> mCommunicators;

    public GameHostService(GameHostListener gameHostListener, List<BluetoothSocket> clients) {
        mCommunicators = new HashMap<>();
        for (BluetoothSocket socket : clients) {
            Communicator communicator = new Communicator(socket, gameHostListener);
            communicator.start();
            mCommunicators.put(socket.getRemoteDevice(), communicator);
        }
    }

    public void runOnAll(Consumer<BluetoothDevice> function) {
        for (BluetoothDevice device : mCommunicators.keySet()) {
            function.accept(device);
        }
    }

    public boolean write(BluetoothDevice device, String message) {
        return mCommunicators.get(device).write(message);
    }

    public void close(BluetoothDevice device) {
        mCommunicators.get(device).close();
        mCommunicators.remove(device);
    }

    public interface GameHostListener extends Communicator.CommunicatorListener {}

}
