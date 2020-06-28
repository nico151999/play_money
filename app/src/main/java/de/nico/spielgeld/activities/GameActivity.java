package de.nico.spielgeld.activities;

import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedHashMap;

import de.nico.spielgeld.R;
import de.nico.spielgeld.views.ClientBluetoothDeviceRecyclerAdapter;

public abstract class GameActivity extends MainActivity {

    private ClientBluetoothDeviceRecyclerAdapter mOpponentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_game);

        findViewById(R.id.coin_five).setOnTouchListener((v, event) -> dragAndDropCoins((ImageView) v, event));
        findViewById(R.id.coin_ten).setOnTouchListener((v, event) -> dragAndDropCoins((ImageView) v, event));
        findViewById(R.id.coin_fifteen).setOnTouchListener((v, event) -> dragAndDropCoins((ImageView) v, event));
        findViewById(R.id.coin_thirty).setOnTouchListener((v, event) -> dragAndDropCoins((ImageView) v, event));
        findViewById(R.id.coin_fifty).setOnTouchListener((v, event) -> dragAndDropCoins((ImageView) v, event));
    }

    private boolean dragAndDropCoins(ImageView view, MotionEvent event) {
        view.performClick();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            ClipData dragData = new ClipData(
                    view.getContentDescription(),
                    new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                    new ClipData.Item(view.getContentDescription())
            );
            View.DragShadowBuilder shadow = new DragAndDropShadowBuilder(view);
            view.setVisibility(View.INVISIBLE);
            view.startDragAndDrop(
                    dragData,
                    shadow,
                    view,
                    View.DRAG_FLAG_OPAQUE
            );
            return true;
        } else {
            return false;
        }
    }

    public abstract void sendMoney(BluetoothDevice device, Integer amount);

    protected String getBluetoothAddress() {
        //return Settings.Secure.getString(this.getContentResolver(), "bluetooth_address"); This does not work without a system level permission called LOCAL_MAC_ADDRESS
        return getBluetoothAdapter().getAddress(); // always returns 02:00:00:00:00:00
    }

    void setOpponentsAdapter(LinkedHashMap<BluetoothDevice, Pair<String, Integer>> accounts) {
        RecyclerView opponentListView = findViewById(R.id.client_list);
        mOpponentsAdapter = new ClientBluetoothDeviceRecyclerAdapter(this, accounts);
        opponentListView.setAdapter(mOpponentsAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        opponentListView.setLayoutManager(layoutManager);
        opponentListView.addItemDecoration(new DividerItemDecoration(opponentListView.getContext(), layoutManager.getOrientation()));
    }

    ClientBluetoothDeviceRecyclerAdapter getOpponentsAdapter() {
        return mOpponentsAdapter;
    }

    private static class DragAndDropShadowBuilder extends View.DragShadowBuilder {
        private Drawable mmShadow;

        private DragAndDropShadowBuilder(ImageView v) {
            super(v);
            mmShadow = v.getDrawable().getConstantState().newDrawable();
        }

        @Override
        public void onProvideShadowMetrics (Point size, Point touch) {
            int width, height;
            width = getView().getWidth();
            height = getView().getHeight();
            mmShadow.setBounds(0, 0, width, height);
            size.set(width, height);
            touch.set(width / 2, height / 2);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            mmShadow.draw(canvas);
        }
    }
}
