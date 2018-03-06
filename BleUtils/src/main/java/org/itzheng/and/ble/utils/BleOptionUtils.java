package org.itzheng.and.ble.utils;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import org.itzheng.and.ble.callback.OnConnectionStateChangeListener;
import org.itzheng.and.ble.callback.OnReceiveDataListener;
import org.itzheng.and.ble.servers.BluetoothLeService;

import java.util.ArrayList;
import java.util.List;

/**
 * Title:蓝牙操作工具类<br>
 * Description: <br>
 * 关于监听回调，应该只保存在本工具类中，每个工具类单独监听服务的监听
 * 有监听回调时，再遍历回调对应的回调列表
 *
 * @email ItZheng@ZoHo.com
 * Created by itzheng on 2018-1-19.
 */
public class BleOptionUtils {
    private static final String TAG = "BleOptionUtils";
    /**
     * 服务未连接时，监听临时保存到这里，等服务器创建时，将监听像服务赋值，然后清空
     */
    private List<OnReceiveDataListener> tempOnReceiveDataListener = new ArrayList<>();
    /**
     * 服务未连接时，监听临时保存到这里，等服务器创建时，将监听像服务赋值，然后清空
     */
    private List<OnConnectionStateChangeListener> tempOnConnectionStateChangeListener = new ArrayList<>();
    private MyOnReceiveDataListener myOnReceiveDataListener = new MyOnReceiveDataListener();
    private MyOnConnectionStateChangeListener myOnConnectionStateChangeListener = new MyOnConnectionStateChangeListener();

    /**
     * 实例化一个对象
     *
     * @return
     */
    public static BleOptionUtils newInstance(Context context) {
        BleOptionUtils bleOptionUtils = new BleOptionUtils();
        bleOptionUtils.init(context);
        return bleOptionUtils;
    }

    /**
     * 蓝牙服务
     */
    private BluetoothLeService mBluetoothLeService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "onServiceConnected: ");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (mBluetoothLeService == null) {
                return;
            }
            addOnReceiveDataListenerToServer();
            addOnConnectionStateChangeListenerToServer();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    /**
     * 获取当前的蓝牙设备
     *
     * @return
     */
    public BluetoothDevice getCurrentDevice() {
        return mBluetoothLeService == null ? null : mBluetoothLeService.getCurrentDevice();
    }

    /**
     * 获取当前的蓝牙地址
     *
     * @return
     */
    public String getCurrentAddress() {
        BluetoothDevice device = getCurrentDevice();
        return device == null ? "" : device.getAddress();
    }

    /**
     * 获取当前的蓝牙名称
     *
     * @return
     */
    public String getCurrentName() {
        BluetoothDevice device = getCurrentDevice();
        return device == null ? "" : device.getName();
    }

    /**
     * 将接收数据的监听添加到服务
     */
    private void addOnReceiveDataListenerToServer() {
        mBluetoothLeService.addOnReceiveDataListener(myOnReceiveDataListener);
    }

    /**
     * 将接收数据的监听添加到服务
     */
    private void addOnConnectionStateChangeListenerToServer() {
        mBluetoothLeService.addOnConnectionStateChangeListener(myOnConnectionStateChangeListener);
    }

    private Context mContext;

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        Log.i(TAG, "init: ");
        mContext = context;
        Intent intent = new Intent(context, BluetoothLeService.class);
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 清空所有引用
     */
    public void recycle() {
        tempOnReceiveDataListener.clear();
        tempOnConnectionStateChangeListener.clear();
        if (mBluetoothLeService != null) {
            //移除服务器的引用
            mBluetoothLeService.removeOnConnectionStateChangeListener(myOnConnectionStateChangeListener);
            mBluetoothLeService.removeOnReceiveDataListener(myOnReceiveDataListener);
        }
    }

    /**
     * 停止蓝牙服务
     */

    public void stopService() {
        if (mBluetoothLeService != null) {
            Intent intent = new Intent(mContext, BluetoothLeService.class);
            mBluetoothLeService.stopService(intent);
        }
    }

    /**
     * 进行蓝牙连接，如果蓝牙不可连接则返回false
     *
     * @param address
     * @return
     */
    public boolean connect(String address) {
        return mBluetoothLeService.connect(address);

    }

    /**
     * 断开连接
     */
    public void disconnect() {
        mBluetoothLeService.disconnect();

    }


    /**
     * 添加收到消息的监听
     *
     * @param listener
     */
    public void addOnReceiveDataListener(OnReceiveDataListener listener) {
        if (listener == null) {
            return;
        }
        tempOnReceiveDataListener.add(listener);
    }

    /**
     * 移除监听
     *
     * @param listener
     */
    public void removeOnReceiveDataListener(OnReceiveDataListener listener) {
        if (listener == null) {
            return;
        }
        tempOnReceiveDataListener.remove(listener);
    }

    /**
     * 添加收到消息的监听
     *
     * @param listener
     */
    public void addOnConnectionStateChangeListener(OnConnectionStateChangeListener listener) {
        if (listener == null) {
            return;
        }
        tempOnConnectionStateChangeListener.add(listener);
    }

    /**
     * 移除监听
     *
     * @param listener
     */
    public void removeOnConnectionStateChangeListener(OnConnectionStateChangeListener listener) {
        if (listener == null) {
            return;
        }
        tempOnConnectionStateChangeListener.remove(listener);
    }

    public int post(byte[] data) {
        return mBluetoothLeService.post(data);
    }

    /**
     * 延时发送，默认50毫秒
     *
     * @param data
     * @return
     */
    public int postDelayedCmd(byte[] data) {
        return postDelayedCmd(data, 50);
    }

    /**
     * 延时发送，将整串的byte一个一个单独发送，
     *
     * @param data    要发送的byte
     * @param sleepMs 间隔时间
     * @return
     */
    public int postDelayedCmd(byte[] data, final long sleepMs) {
        final byte[] finalBytes = data;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < finalBytes.length; i++) {
                    post(new byte[]{finalBytes[i]});
                    SystemClock.sleep(sleepMs);
                }
            }
        }).start();
        return 1;
    }

    /**
     * 当前是否处于连接状态
     *
     * @return
     */
    public boolean isConnect() {
        return mBluetoothLeService != null && mBluetoothLeService.isConnect();
    }

    /**
     * 接收服务的消息回调
     */
    private class MyOnReceiveDataListener implements OnReceiveDataListener {

        @Override
        public void onReceiveData(byte[] value) {
            updateReceiveData(value);
        }
    }

    /**
     * 更新接收消息的监听
     *
     * @param value
     */
    private void updateReceiveData(byte[] value) {
        for (int i = 0; i < tempOnReceiveDataListener.size(); i++) {
            OnReceiveDataListener onReceiveDataListener = tempOnReceiveDataListener.get(i);
            onReceiveDataListener.onReceiveData(value);
        }
    }

    /**
     * 连接状态监听
     */
    private class MyOnConnectionStateChangeListener implements OnConnectionStateChangeListener {

        @Override
        public void onConnected() {
            updateConnectionState(BluetoothLeService.STATE_CONNECTED);
        }

        @Override
        public void onDisconnected() {
            updateConnectionState(BluetoothLeService.STATE_DISCONNECTED);
        }

        @Override
        public void onServicesDiscovered() {
            updateConnectionState(BluetoothLeService.STATE_SERVICES_DISCOVERED);
        }
    }

    /**
     * 更新连接状态
     *
     * @param state
     */
    private void updateConnectionState(int state) {
        for (int i = 0; i < tempOnConnectionStateChangeListener.size(); i++) {
            OnConnectionStateChangeListener listener = tempOnConnectionStateChangeListener.get(i);
            switch (state) {
                case BluetoothLeService.STATE_CONNECTED:
                    listener.onConnected();
                    break;
                case BluetoothLeService.STATE_DISCONNECTED:
                    listener.onDisconnected();
                    break;
                case BluetoothLeService.STATE_SERVICES_DISCOVERED:
                    listener.onServicesDiscovered();
                    break;
            }
        }
    }
}
