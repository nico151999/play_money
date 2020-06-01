package de.nico.spielgeld.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import de.nico.spielgeld.R;

public abstract class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 5647;
    private static final int REQUEST_MAKE_DISCOVERABLE = 868;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mReceiver;
    private static final int PERMISSION_REQUEST_CODE = 516;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mReceiver = new BluetoothIntentReceiver();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetooth_not_available, Toast.LENGTH_LONG).show();
            finish();
        }

        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_UUID));

        List<String> permissions = getUnGrantedPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        if (permissions.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissions.toArray(new String[]{}),
                    PERMISSION_REQUEST_CODE
            );
        }

        if (!mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode != AppCompatActivity.RESULT_OK) {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                break;
            case REQUEST_MAKE_DISCOVERABLE:
                onDiscoverabilityPromptResult(resultCode != AppCompatActivity.RESULT_CANCELED);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private List<String> getUnGrantedPermissions(String[] permissions) {
        ArrayList<String> ungranted = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                ungranted.add(permission);
            }
        }
        return ungranted;
    }

    private class BluetoothIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) == BluetoothAdapter.STATE_TURNING_OFF) {
                            Toast.makeText(MainActivity.this, R.string.bluetooth_disabled, Toast.LENGTH_LONG).show();
                            finish();
                        }
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        onBluetoothDiscoveryDeviceFound(device);
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        onBluetoothDiscoveryFinished();
                        break;
                    case BluetoothDevice.ACTION_UUID:
                        onUuidFetched(
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE),
                                intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                        );
                        break;
                }
            }
        }
    }

    protected void promptForDiscoverability() {
        Intent discoverableIntent =  new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
        startActivityForResult(discoverableIntent, REQUEST_MAKE_DISCOVERABLE);
    }
    protected void promptForUuidFetch(BluetoothDevice device) {
        device.fetchUuidsWithSdp();
    }
    protected BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }
    protected void onBluetoothDiscoveryDeviceFound(BluetoothDevice device) {}
    protected void onBluetoothDiscoveryFinished() {}
    protected void onDiscoverabilityPromptResult(boolean discoverable) {}
    protected void onUuidFetched(BluetoothDevice device, Parcelable[] uuids) {}
}