package org.itzheng.and.ble.filter;

import android.bluetooth.BluetoothDevice;

/**
 * Title:<br>
 * Description: <br>
 *
 * @email ItZheng@ZoHo.com
 * Created by itzheng on 2018-3-14.
 */
public interface BleScanFilter {
    boolean isAdd(BluetoothDevice device, int rssi, byte[] scanRecord);
}
