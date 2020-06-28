package de.nico.spielgeld.views;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.util.Pair;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import de.nico.spielgeld.Constants;
import de.nico.spielgeld.R;
import de.nico.spielgeld.activities.GameActivity;

public class ClientBluetoothDeviceRecyclerAdapter extends RecyclerView.Adapter<ClientBluetoothDeviceRecyclerAdapter.ViewHolder> {

    private LinkedHashMap<BluetoothDevice, Pair<String, Integer>> mAccounts;
    private GameActivity mContext;

    public ClientBluetoothDeviceRecyclerAdapter(GameActivity context, LinkedHashMap<BluetoothDevice, Pair<String, Integer>> accounts) {
        mContext = context;
        mAccounts = accounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClientBluetoothDeviceRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ClientBluetoothDeviceRecyclerAdapter.ViewHolder(
                (LinearLayout) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_client_bluetooth_device, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ClientBluetoothDeviceRecyclerAdapter.ViewHolder holder, int position) {
        Map.Entry<BluetoothDevice, Pair<String, Integer>> set = (Map.Entry<BluetoothDevice, Pair<String, Integer>>) mAccounts.entrySet().toArray()[position];
        BluetoothDevice device = set.getKey();
        String text = set.getValue().first;
        Integer account = set.getValue().second;
        ((MaterialTextView) holder.mmDeviceView.getChildAt(0)).setText(text == null ? mContext.getString(R.string.unknown_device) : text);
        ((MaterialTextView) holder.mmDeviceView.getChildAt(1)).setText(account == null ? Constants.INITIAL_ACCOUNT.toString() : account.toString());
        holder.mmDeviceView.setOnDragListener((view, dragEvent) -> {
            switch (dragEvent.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                case DragEvent.ACTION_DROP:
                    mContext.sendMoney(
                            device,
                            Integer.parseInt(
                                    dragEvent.getClipData().getItemAt(0).getText().toString()
                            )
                    );
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    holder.mmDeviceView.setBackgroundColor(Color.GRAY);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    ImageView imageView = (ImageView) dragEvent.getLocalState();
                    if (imageView.getVisibility() == View.INVISIBLE) {
                        imageView.setVisibility(View.VISIBLE);
                    }
                case DragEvent.ACTION_DRAG_EXITED:
                    holder.mmDeviceView.setBackgroundColor(Color.WHITE);
                    return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return mAccounts.size();
    }

    public void remove(BluetoothDevice device) {
        mAccounts.remove(device);
        notifyDataSetChanged();
    }

    public Integer update(BluetoothDevice device, Integer delta) {
        Pair<String, Integer> oldEntry = mAccounts.get(device);
        Integer newValue = oldEntry.second + delta;
        mAccounts.put(device, new Pair<>(oldEntry.first, newValue));
        notifyItemChanged(new ArrayList<>(mAccounts.keySet()).indexOf(device));
        return newValue;
    }

    public LinkedHashMap<BluetoothDevice, Pair<String, Integer>> getAccounts() {
        return mAccounts;
    }

    public void updateTotal(BluetoothDevice device, Integer account) {
        mAccounts.replace(device, new Pair<>(mAccounts.get(device).first, account));
        notifyItemChanged(new ArrayList<>(mAccounts.keySet()).indexOf(device));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mmDeviceView;
        private ViewHolder(LinearLayout v) {
            super(v);
            mmDeviceView = v;
        }
    }
}