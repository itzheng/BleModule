package org.itzheng.and.ble.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.itzheng.and.ble.callback.OnBleSwitchStatusChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Title:蓝牙开关状态工具，用于监听蓝牙状态<br>
 * Description: <br>
 *
 * @email ItZheng@ZoHo.com
 * Created by itzheng on 2018-6-11.
 */
public class BleSwitchStatusUtils {
    private Context mContext;

    public BleSwitchStatusUtils(Context context) {
        mContext = context;
    }

    public static BleSwitchStatusUtils newInstance(Context context) {
        return new BleSwitchStatusUtils(context);
    }

    /**
     * 监听列表
     */
    private List<OnBleSwitchStatusChangeListener> mOnBleSwitchStatusChangeListeners = new ArrayList<>();

    /**
     * 添加蓝牙开关监听
     *
     * @param onBleSwitchStatusChangeListener
     */
    public void addOnBleSwitchStatusChangeListener(OnBleSwitchStatusChangeListener onBleSwitchStatusChangeListener) {
        if (onBleSwitchStatusChangeListener != null) {
            //添加之前先注册监听

            if (mOnBleSwitchStatusChangeListeners.isEmpty()) {
                registerReceiver();
            }
            //避免重复添加
            if (!mOnBleSwitchStatusChangeListeners.contains(onBleSwitchStatusChangeListener))
                mOnBleSwitchStatusChangeListeners.add(onBleSwitchStatusChangeListener);
        }
    }

    /**
     * 移除蓝牙状态更改监听
     *
     * @param onBleSwitchStatusChangeListener
     */
    public void removeOnBleSwitchStatusChangeListener(OnBleSwitchStatusChangeListener onBleSwitchStatusChangeListener) {
        if (onBleSwitchStatusChangeListener != null) {
            mOnBleSwitchStatusChangeListeners.remove(onBleSwitchStatusChangeListener);
        }
        //当没有监听时，则移除
        if (mOnBleSwitchStatusChangeListeners.isEmpty()) {
            unregisterReceiver();
        }
    }

    /**
     * 解除注册广播监听
     */
    private void unregisterReceiver() {
        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 注册广播监听
     */
    private void registerReceiver() {
        try {
            mContext.registerReceiver(mReceiver, makeFilter());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    updateReceiveData(blueState);
                    break;
            }
        }
    };

    private void updateReceiveData(int blueState) {
        switch (blueState) {
            case BluetoothAdapter.STATE_TURNING_ON:
                break;
            case BluetoothAdapter.STATE_ON:
                //蓝牙打开
                for (int i = 0; i < mOnBleSwitchStatusChangeListeners.size(); i++) {
                    OnBleSwitchStatusChangeListener onReceiveDataListener = mOnBleSwitchStatusChangeListeners.get(i);
                    onReceiveDataListener.onStateOn();
                }
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                //蓝牙关闭
                for (int i = 0; i < mOnBleSwitchStatusChangeListeners.size(); i++) {
                    OnBleSwitchStatusChangeListener onReceiveDataListener = mOnBleSwitchStatusChangeListeners.get(i);
                    onReceiveDataListener.onStateOff();
                }
                break;
            case BluetoothAdapter.STATE_OFF:
                break;
        }
    }

    /**
     * 进行内存回收
     */
    public void recycle() {
        mOnBleSwitchStatusChangeListeners.clear();
        unregisterReceiver();
    }
}
