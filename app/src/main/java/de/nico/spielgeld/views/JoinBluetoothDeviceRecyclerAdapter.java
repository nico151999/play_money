package de.nico.spielgeld.views;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import de.nico.spielgeld.R;
import de.nico.spielgeld.activities.JoinGameActivity;

public class JoinBluetoothDeviceRecyclerAdapter extends RecyclerView.Adapter<JoinBluetoothDeviceRecyclerAdapter.ViewHolder> {

    private LinkedHashMap<BluetoothDevice, Boolean> mDevices = new LinkedHashMap<>();
    private JoinGameActivity mContext;

    public JoinBluetoothDeviceRecyclerAdapter(JoinGameActivity activity) {
        mContext = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_join_bluetooth_device, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<BluetoothDevice, Boolean> set = (Map.Entry<BluetoothDevice, Boolean>) mDevices.entrySet().toArray()[position];
        BluetoothDevice device = set.getKey();
        boolean isAppDevice = set.getValue();
        String text = device.getName();
        if (isAppDevice || mContext.isShowAllEnabled()) {
            ((MaterialTextView) holder.mmDeviceView.getChildAt(0)).setText(text == null ? mContext.getString(R.string.unknown_device) : text);
            holder.mmDeviceView.getChildAt(1).setOnClickListener((view) -> mContext.connectToServer(device));
            holder.mmDeviceView.setVisibility(View.VISIBLE);
            holder.mmDeviceView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            holder.mmDeviceView.setVisibility(View.GONE);
            holder.mmDeviceView.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        }
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void add(BluetoothDevice device, Boolean appDevice) {
        if (!mDevices.containsKey(device)) {
            mDevices.put(device, appDevice);
            notifyItemInserted(mDevices.size() - 1);
        }
    }

    public void setIsAppDevice(BluetoothDevice device) {
        if (mDevices.replace(device, true) != null) {
            notifyItemChanged(new ArrayList<>(mDevices.keySet()).indexOf(device));
        }
    }

    public BluetoothDevice getBluetoothDevice(int i) {
        return ((Map.Entry<BluetoothDevice, Boolean>) mDevices.entrySet().toArray()[i]).getKey();
    }

    public void clear() {
        mDevices.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mmDeviceView;
        private ViewHolder(LinearLayout v) {
            super(v);
            mmDeviceView = v;
        }
    }
}
