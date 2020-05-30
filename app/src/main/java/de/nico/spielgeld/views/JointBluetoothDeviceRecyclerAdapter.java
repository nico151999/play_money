package de.nico.spielgeld.views;

import android.bluetooth.BluetoothSocket;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import de.nico.spielgeld.R;
import de.nico.spielgeld.activities.CreateGameActivity;

public class JointBluetoothDeviceRecyclerAdapter extends RecyclerView.Adapter<JointBluetoothDeviceRecyclerAdapter.ViewHolder> {

    private List<BluetoothSocket> mDevices;
    private CreateGameActivity mContext;

    public JointBluetoothDeviceRecyclerAdapter(CreateGameActivity activity) {
        if (mDevices == null) {
            mDevices = new ArrayList<>();
        }
        mContext = activity;
    }

    @NonNull
    @Override
    public JointBluetoothDeviceRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_joint_bluetooth_device, parent, false);
        return new JointBluetoothDeviceRecyclerAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull JointBluetoothDeviceRecyclerAdapter.ViewHolder holder, int position) {
        String text = mDevices.get(position).getRemoteDevice().getName();
        ((MaterialTextView) holder.mmDeviceView.getChildAt(0)).setText(text == null ? mContext.getString(R.string.unknown_device) : text);
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public List<BluetoothSocket> getClients() {
        return mDevices;
    }

    public void add(BluetoothSocket device) {
        if (!mDevices.contains(device)) {
            mDevices.add(device);
            notifyItemInserted(mDevices.size() - 1);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mmDeviceView;
        private ViewHolder(LinearLayout v) {
            super(v);
            mmDeviceView = v;
        }
    }
}