package org.itzheng.and.ble.callback;

import android.bluetooth.BluetoothDevice;

import org.itzheng.and.ble.bean.BluetoothDeviceInfo;

import java.util.List;

/**
 * Title:<br>
 * Description: <br>
 *
 * @email ItZheng@ZoHo.com
 * Created by itzheng on 2018-1-19.
 */
public interface BleScanCallback {

    public void onLeScan(List<BluetoothDeviceInfo> deviceInfoList);

    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord);
}
