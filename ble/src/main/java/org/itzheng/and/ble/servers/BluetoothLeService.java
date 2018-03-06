//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.itzheng.and.ble.servers;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import org.itzheng.and.ble.utils.BluetoothUtils;
import org.itzheng.and.ble.utils.ByteUtils;
import org.itzheng.and.ble.uuid.DefUuid;
import org.itzheng.and.ble.uuid.IUUIDS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 蓝牙服务器，一个蓝牙服务同一时间，只能连接一个蓝牙
 * 如果需要支持多蓝牙需要进行缓存判断：
 * 1，需要对BluetoothGattCallback进行多个判断
 * 2，建立连接时，不要断开当前连接，新建一个连接
 * 3，连接成功后，需要将连接对象进行缓存，断开连接需要移除
 * 还有很多细节需要考虑，目前暂无多蓝牙连接需求，先不考虑
 */
public class BluetoothLeService extends Service {
    private static final String TAG = "BluetoothLeService";
    /**
     * 发现服务完成的监听
     */
    public final static int STATE_SERVICES_DISCOVERED = 0xd1;
    /**
     * The profile is in disconnected state
     */
    public static final int STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED;
    /**
     * The profile is in connecting state
     */
    public static final int STATE_CONNECTING = BluetoothProfile.STATE_CONNECTING;
    /**
     * The profile is in connected state
     */
    public static final int STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;
    /**
     * The profile is in disconnecting state
     */
    public static final int STATE_DISCONNECTING = BluetoothProfile.STATE_DISCONNECTING;

    private BluetoothGatt mBluetoothGatt;
    /**
     * 蓝牙连接状态
     * String 蓝牙地址
     * Integer 连接状态 {@link android.bluetooth.BluetoothProfile}
     */
    private Map<String, Integer> mBleConnectionStatus = new HashMap<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    Handler mHandler = new Handler(Looper.getMainLooper());
    //多蓝牙通信的话，只要重写这个监听，就可以分别接受消息了
    MyBluetoothGattCallback mGattCallback = new MyBluetoothGattCallback();
    /**
     * 收到消息的监听
     */
    private List<OnReceiveDataListener> mOnReceiveDataListeners = new ArrayList<>();
    /**
     * 状态改变的监听
     */
    private List<OnConnectionStateChangeListener> mOnConnectionStateChangeListeners = new ArrayList<>();
    /**
     * 最新连接的蓝牙地址
     */
    private String mBleAddress = "";

    /**
     * 连接蓝牙到指定地址
     *
     * @param address
     * @return
     */
    @SuppressLint("NewApi")
    public boolean connect(String address) {
        mBleAddress = address;
        Log.d(TAG, "connect:" + address);
        BluetoothAdapter bluetoothAdapter = BluetoothUtils.getBluetoothAdapter();
        if (bluetoothAdapter == null) {
            //蓝牙不可用
            return false;
        }
        if (false && mBluetoothGatt != null) {
//            BluetoothDevice bluetoothDevice = mBluetoothGatt.getDevice();
//            if (bluetoothDevice != null) {
//                if (address.equalsIgnoreCase(bluetoothDevice.getAddress())) {
//                    if (mBluetoothGatt.connect()) {
//                        //如果当前连接的蓝牙和目标一致，就不用重新连接,
//                        //如果进行再次连接会造成重复连接，则数据接收重复
//                        Log.i(TAG, "mBluetoothGatt.connect()");
//                        return true;
//                    }
//                }
//            }
            //如果之前连接的是其他设备，将其他设备断开连接,
            // 如果未断开，会同时连接多个蓝牙
            //目前没有对多个连接进行处理
//            mBluetoothGatt.disconnect();
            disconnect();
        }
        //每次进行连接前，先断开连接
        disconnect();
        if (true) {
            //新建一个连接
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                Log.w(TAG, "Device not found.  Unable to connect.");
                return false;
            } else {
                mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
                Log.d(TAG, "Trying to create a new connection.");
                return true;
            }
        }
        return true;
    }

    /**
     * 获取当前的连接设备
     *
     * @return
     */
    @SuppressLint("NewApi")
    public BluetoothDevice getCurrentDevice() {
        return mBluetoothGatt == null ? null : mBluetoothGatt.getDevice();
    }

    /**
     * 断开当前的蓝牙连接
     */
    @SuppressLint("NewApi")
    public void disconnect() {
        if (mBluetoothGatt != null && mBluetoothGatt.connect()) {
            mBluetoothGatt.disconnect();
        }
    }

    /**
     * 发送数据的长度，最大长度为20，系统不同，长度就不同，当前版本设置为1，一次发送1个字节
     */
    private static final int post_value_length = 20;

    @SuppressLint("NewApi")
    public int post(byte[] writeBytes) {
        Log.i(TAG, "post: " + ByteUtils.toHexString(writeBytes));
        enableUuid();
        int error = -1;
        if (mBluetoothGatt == null) {
            Log.w(TAG, "mBluetoothGatt is null");
            return error;
        }
        BluetoothGattService gattService = mBluetoothGatt.getService(UUID.fromString(mUUids.getServiceUuid()));
        if (gattService == null) {
            Log.w(TAG, "Service is null");
            return error;
        }
        BluetoothGattCharacteristic gg = gattService.getCharacteristic(UUID.fromString(mUUids.getCharacteristicUuid()));
        if (gg == null) {
            Log.w(TAG, "gg is null");
            return error;
        }
        int ic = 0;
        int length = writeBytes.length;
        int data_len_20 = length / post_value_length;
        int data_len_0 = length % post_value_length;
        int i = 0;
        byte[] da;
        int h;
        //整数发送的数据
        if (data_len_20 > 0) {
            while (i < data_len_20) {
                da = new byte[post_value_length];
                for (h = 0; h < post_value_length; ++h) {
                    da[h] = writeBytes[post_value_length * i + h];
                }
                gg.setValue(da);
                this.mBluetoothGatt.writeCharacteristic(gg);
                ic += post_value_length;
                ++i;
            }
        }
        //上面不能发送完成的数据，继续发送
        if (data_len_0 > 0) {
            da = new byte[data_len_0];
            for (h = 0; h < data_len_0; ++h) {
                da[h] = writeBytes[post_value_length * i + h];
            }
            gg.setValue(da);
            this.mBluetoothGatt.writeCharacteristic(gg);
            ic += data_len_0;
        }

        return ic;
    }

    /**
     * 向蓝牙发送数据
     *
     * @param g
     * @param string_or_hex_data 是字符串，还是16进制 true：字符串 false 16进制
     * @return
     */
    @SuppressLint("NewApi")
    public int post(String g, boolean string_or_hex_data) {
        byte[] writeBytes = null;
        if (string_or_hex_data) {
            writeBytes = g.getBytes();
        } else {
            writeBytes = ByteUtils.getBytesByString(g);
        }
        return post(writeBytes);
    }

    /**
     * 蓝牙是否连接，当前的地址
     *
     * @return
     */
    public boolean isConnect() {
        return isConnect(mBleAddress);
    }

    /**
     * 判断是否连接指定蓝牙
     *
     * @param address
     * @return
     */
    public boolean isConnect(String address) {
        if (address == null || mBleConnectionStatus.isEmpty()) {
            return false;
        }
        Integer integer = mBleConnectionStatus.get(address);
        if (integer == null) {
            return false;
        }
        return integer == BluetoothProfile.STATE_CONNECTED;
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @SuppressLint("NewApi")
    public List<BluetoothGattService> getSupportedGattServices() {
        return this.mBluetoothGatt == null ? null : this.mBluetoothGatt.getServices();
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        if (gattServices.size() > 0 && getConnectedStatus(gattServices)) {
            enableBle();
        } else {
            Log.e(TAG, "displayGattServices: " + " can't enableBle");
        }
    }

    /**
     * 判断服务是否处于连接状态
     *
     * @param gattServices
     * @return
     */
    @SuppressLint("NewApi")
    private boolean getConnectedStatus(List<BluetoothGattService> gattServices) {
        boolean jdy_ble_server = false;
        boolean jdy_ble_ffe = false;
        String uuid = null;
        Iterator var14 = gattServices.iterator();
        enableUuid();
        while (var14.hasNext()) {
            BluetoothGattService gattService = (BluetoothGattService) var14.next();
            uuid = gattService.getUuid().toString();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            if (mUUids.getServiceUuid().equals(uuid)) {
                jdy_ble_server = true;
            }
            Iterator var20 = gattCharacteristics.iterator();
            while (var20.hasNext()) {
                BluetoothGattCharacteristic gattCharacteristic = (BluetoothGattCharacteristic) var20.next();
                uuid = gattCharacteristic.getUuid().toString();
                if (jdy_ble_server) {
                    if (mUUids.getCharacteristicUuid().equalsIgnoreCase(uuid)) {
                        jdy_ble_ffe = true;
                    }
                }
            }
        }
        //如果server未连接，ffe是不可能连接的，所以，如果ffe为连接状态，则server肯定为连接状态
        return jdy_ble_ffe;
    }

    private IUUIDS mUUids;

    /**
     * 设置UUID
     *
     * @param uuids
     */
    public void setUUids(IUUIDS uuids) {
        mUUids = uuids;
    }

    @SuppressLint("NewApi")
    private void enableBle() {
        enableUuid();
        try {
            BluetoothGattService service = this.mBluetoothGatt.getService(UUID.fromString(mUUids.getServiceUuid()));
            BluetoothGattCharacteristic ale;
            ale = service.getCharacteristic(UUID.fromString(mUUids.getCharacteristicUuid()));
            //设置接收通知
            this.mBluetoothGatt.setCharacteristicNotification(ale, true);
            //设置完接收通知后，发送一条确认消息
            BluetoothGattDescriptor dsc = ale.getDescriptor(UUID.fromString(mUUids.getDescriptorUUid()));
            byte[] bytes = new byte[]{1, 0};
            dsc.setValue(bytes);
            this.mBluetoothGatt.writeDescriptor(dsc);
        } catch (NumberFormatException var8) {
            var8.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private class MyBluetoothGattCallback extends BluetoothGattCallback {
        /**
         * 连接状态改变时的监听
         *
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            //添加连接状态
            mBleConnectionStatus.put(gatt.getDevice().getAddress(), newState);
            Log.i(TAG, "onConnectionStateChange status:" + status + ",newState:" + newState);
            switch (newState) {
                case BluetoothAdapter.STATE_CONNECTED:
                    //连接蓝牙
                    //进行发现服务
                    if (mBluetoothGatt != null) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mBluetoothGatt.discoverServices();
                            }
                        }, 0);
                    }

                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    //蓝牙断开
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt.close();
//                        disconnect();
//                        mBluetoothGatt = null;
                    }
                    break;
            }
            updateConnectionStateListener(newState);
        }

        /**
         * 服务发现时的监听
         *
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i(TAG, "onServicesDiscovered: " + status);
            if (status == 0) {
                //服务发现后，需要设置接收通知消息
                displayGattServices(getSupportedGattServices());
            }
            updateConnectionStateListener(STATE_SERVICES_DISCOVERED);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(TAG, "onCharacteristicRead: ");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
//            Log.d(TAG, "onCharacteristicChanged: value: " + ByteUtils.toHexString(characteristic.getValue()));
            UUID characteristicUuid = characteristic.getUuid();
            enableUuid();
            if (UUID.fromString(mUUids.getCharacteristicUuid()).equals(characteristicUuid)) {
//                Log.d(TAG, "onCharacteristicChanged: value: " + ByteUtils.toHexString(characteristic.getValue()));
                postOnReceiveData(characteristic.getValue());
            }
        }
    }


    /**
     * 更新连接状态
     *
     * @param newState
     */
    private void updateConnectionStateListener(int newState) {
        for (int i = 0; i < mOnConnectionStateChangeListeners.size(); i++) {
            OnConnectionStateChangeListener listener = mOnConnectionStateChangeListeners.get(i);
            if (listener == null) {
                mOnConnectionStateChangeListeners.remove(i);
                i--;
                continue;
            }
            switch (newState) {
                //已连接
                case STATE_CONNECTED:
                    listener.onConnected();
                    break;
                case STATE_DISCONNECTED:
                    //蓝牙断开
                    listener.onDisconnected();
                    break;
                case STATE_SERVICES_DISCOVERED:
                    listener.onServicesDiscovered();
                    break;
            }
        }

    }

    /**
     * 添加蓝牙连接状态监听，可以添加多个
     *
     * @param listener
     */
    public void addOnConnectionStateChangeListener(OnConnectionStateChangeListener listener) {
        if (listener != null) {
            if (mOnConnectionStateChangeListeners.contains(listener)) {
                mOnConnectionStateChangeListeners.remove(listener);
            }
            mOnConnectionStateChangeListeners.add(listener);
        }
    }

    /**
     * 移除蓝牙连接状态监听
     *
     * @param listener
     */
    public void removeOnConnectionStateChangeListener(OnConnectionStateChangeListener listener) {
        if (listener != null) {
            mOnConnectionStateChangeListeners.remove(listener);
        }
    }

    /**
     * 将收到的数据发送给需要的监听者
     *
     * @param value
     */
    private void postOnReceiveData(byte[] value) {
        for (int i = 0; i < mOnReceiveDataListeners.size(); i++) {
            OnReceiveDataListener onReceiveDataListener = mOnReceiveDataListeners.get(i);
            if (onReceiveDataListener == null) {
                mOnReceiveDataListeners.remove(i);
                i--;
            } else {
                onReceiveDataListener.onReceiveData(value);
            }
        }

    }


    /**
     * 添加数据接收回调，可以添加多个
     *
     * @param onReceiveDataListener
     */
    public void addOnReceiveDataListener(OnReceiveDataListener onReceiveDataListener) {
        if (onReceiveDataListener != null) {
            if (mOnReceiveDataListeners.contains(onReceiveDataListener)) {
                mOnReceiveDataListeners.remove(onReceiveDataListener);
            }
            mOnReceiveDataListeners.add(onReceiveDataListener);
        }
    }

    /**
     * 移除接收数据的监听
     *
     * @param onReceiveDataListener
     */
    public void removeOnReceiveDataListener(OnReceiveDataListener onReceiveDataListener) {
        if (onReceiveDataListener != null) {
            mOnReceiveDataListeners.remove(onReceiveDataListener);
        }
    }

    /**
     * 收到数据的监听回调
     */
    public interface OnReceiveDataListener {
        /**
         * 收到的数据
         *
         * @param value
         */
        void onReceiveData(byte[] value);
    }

    /**
     * 连接状态改变时的监听
     */
    public interface OnConnectionStateChangeListener {
        /**
         * 连接时的监听
         */
        void onConnected();

        /**
         * 断开连接的监听
         */
        void onDisconnected();

        /**
         * 服务扫描完成后的监听
         */
        void onServicesDiscovered();

    }

    /**
     * 判断uuid是否可用，如果不可以使用默认的uuid
     */
    private void enableUuid() {
        if (mUUids == null) {
            mUUids = new DefUuid();
        }
    }
}
