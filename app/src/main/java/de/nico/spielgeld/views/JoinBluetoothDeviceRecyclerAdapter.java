package de.nico.spielgeld.views;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import de.nico.spielgeld.R;
import de.nico.spielgeld.activities.JoinGameActivity;

public class JoinBluetoothDeviceRecyclerAdapter extends RecyclerView.Adapter<JoinBluetoothDeviceRecyclerAdapter.ViewHolder> {

    private List<BluetoothDevice> mDevices = new ArrayList<>();
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
        BluetoothDevice device = mDevices.get(position);
        String text = device.getName();
        ((MaterialTextView) holder.mmDeviceView.getChildAt(0)).setText(text == null ? mContext.getString(R.string.unknown_device) : text);
        holder.mmDeviceView.getChildAt(1).setOnClickListener((view) -> mContext.connectToServer(device));
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void add(BluetoothDevice device) {
        if (!mDevices.contains(device)) {
            mDevices.add(device);
            notifyItemInserted(mDevices.size() - 1);
        }
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
