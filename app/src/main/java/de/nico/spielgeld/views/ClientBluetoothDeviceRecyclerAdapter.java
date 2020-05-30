package de.nico.spielgeld.views;

import android.bluetooth.BluetoothDevice;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import de.nico.spielgeld.Constants;
import de.nico.spielgeld.R;
import de.nico.spielgeld.activities.GameActivity;

public class ClientBluetoothDeviceRecyclerAdapter extends RecyclerView.Adapter<ClientBluetoothDeviceRecyclerAdapter.ViewHolder> {

    private LinkedHashMap<BluetoothDevice, Pair<String, Double>> mAccounts;
    private GameActivity mContext;

    public ClientBluetoothDeviceRecyclerAdapter(GameActivity context, LinkedHashMap<BluetoothDevice, Pair<String, Double>> accounts) {
        mContext = context;
        mAccounts = accounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClientBluetoothDeviceRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client_bluetooth_device, parent, false);
        return new ClientBluetoothDeviceRecyclerAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientBluetoothDeviceRecyclerAdapter.ViewHolder holder, int position) {
        Map.Entry<BluetoothDevice, Pair<String, Double>> set = (Map.Entry<BluetoothDevice, Pair<String, Double>>) mAccounts.entrySet().toArray()[position];
        BluetoothDevice device = set.getKey();
        String text = set.getValue().first;
        Double account = set.getValue().second;
        LinearLayout rowOne = (LinearLayout) holder.mmDeviceView.getChildAt(0);
        LinearLayout rowTwo = (LinearLayout) holder.mmDeviceView.getChildAt(1);
        ((MaterialTextView) rowOne.getChildAt(0)).setText(text == null ? mContext.getString(R.string.unknown_device) : text);
        ((MaterialTextView) rowOne.getChildAt(1)).setText(account == null ? Constants.INITIAL_ACCOUNT.toString() : account.toString());
        rowTwo.getChildAt(0).setOnClickListener((view) -> mContext.sendMoney(device, Double.parseDouble(((MaterialButton) view).getText().toString())));
        rowTwo.getChildAt(1).setOnClickListener((view) -> mContext.sendMoney(device, Double.parseDouble(((MaterialButton) view).getText().toString())));
        rowTwo.getChildAt(2).setOnClickListener((view) -> mContext.sendMoney(device, Double.parseDouble(((MaterialButton) view).getText().toString())));
        rowTwo.getChildAt(3).setOnClickListener((view) -> mContext.sendMoney(device, Double.parseDouble(((MaterialButton) view).getText().toString())));
        rowTwo.getChildAt(4).setOnClickListener((view) -> mContext.sendMoney(device, Double.parseDouble(((MaterialButton) view).getText().toString())));
    }

    @Override
    public int getItemCount() {
        return mAccounts.size();
    }

    public void remove(BluetoothDevice device) {
        mAccounts.remove(device);
        notifyDataSetChanged();
    }

    public Double update(BluetoothDevice device, Double delta) {
        Pair<String, Double> oldEntry = mAccounts.get(device);
        Double newValue = oldEntry.second + delta;
        mAccounts.put(device, new Pair<>(oldEntry.first, newValue));
        notifyItemChanged(new ArrayList<>(mAccounts.keySet()).indexOf(device));
        return newValue;
    }

    public LinkedHashMap<BluetoothDevice, Pair<String, Double>> getAccounts() {
        return mAccounts;
    }

    public void updateTotal(BluetoothDevice device, Double account) {
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