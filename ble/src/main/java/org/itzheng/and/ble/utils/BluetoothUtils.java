package org.itzheng.and.ble.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

/**
 * 需要权限：
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 * Created by admin on 2017/7/16.
 */

public class BluetoothUtils {
    private static BluetoothAdapter mBluetoothAdapter;

    /**
     * 是否支持蓝牙模块
     *
     * @return true 支持，false 不支持
     */
    public static boolean hasBluetoothAdapter() {
        return getBluetoothAdapter() != null;
    }

    /**
     * 获取默认的蓝牙适配器
     *
     * @return
     */
    public static BluetoothAdapter getBluetoothAdapter() {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return mBluetoothAdapter;
    }

    /**
     * 蓝牙是否可用，是否打开
     *
     * @return
     */
    public static boolean isEnabled() {
        if (hasBluetoothAdapter()) {
            return mBluetoothAdapter.isEnabled();
        } else {
            return false;
        }
    }

    /**
     * 关闭蓝牙
     *
     * @return
     */
    public static boolean disable() {
        if (hasBluetoothAdapter()) {
            return mBluetoothAdapter.disable();
        } else {
            return false;
        }
    }

    /**
     * 是否正在扫描
     *
     * @return
     */
    public static boolean isDiscovering() {
        if (hasBluetoothAdapter()) {
            return mBluetoothAdapter.isDiscovering();
        } else {
            return false;
        }
    }

    /**
     * 停止扫描
     *
     * @return
     */
    public static boolean cancelDiscovery() {
        if (hasBluetoothAdapter()) {
            return mBluetoothAdapter.cancelDiscovery();
        } else {
            return false;
        }
    }

    /**
     * 开始搜索蓝牙
     *
     * @return
     */
    public static boolean startDiscovery() {
        if (hasBluetoothAdapter()) {
            return mBluetoothAdapter.startDiscovery();
        } else {
            return false;
        }
    }

    /**
     * 获取名字
     *
     * @return
     */
    public static String getName() {
        if (hasBluetoothAdapter()) {
            return mBluetoothAdapter.getName();
        }
        return "";
    }

    /**
     * 获取mac地址
     *
     * @return
     */
    public static String getAddress() {
        if (hasBluetoothAdapter()) {
            return mBluetoothAdapter.getAddress();
        }
        return "";
    }

    /**
     * 判断状态
     *
     * @return BluetoothAdapter.STATE_ON 蓝牙已经打开, BluetoothAdapter.STATE_TURNING_ON 蓝牙正在打开,
     * BluetoothAdapter.STATE_TURNING_OFF 蓝牙正在关闭,
     * BluetoothAdapter.STATE_OFF 蓝牙已经关闭
     */
    public static int getState() {
        if (hasBluetoothAdapter()) {
            return mBluetoothAdapter.getState();
        }
        return 0;
    }

    public static void open(Activity activity) {
        if (hasBluetoothAdapter()) {
            if (mBluetoothAdapter.isEnabled()) {
                //已经打开
            } else {
                Intent open = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(open, REQUEST_OPEN);
            }
        } else {
            //没有蓝牙模块
        }
    }

    public static final int REQUEST_OPEN = 0xcd01;//打开一个蓝牙

    /**
     * 在onActivityResult中判断requestCode，如果相等，就判断打开状态
     *
     * @return
     */
    public static int getOpenRequestCode() {
        return REQUEST_OPEN;
    }

}
