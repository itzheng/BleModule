package org.itzheng.and.ble.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.support.annotation.RequiresApi;

import org.itzheng.and.ble.bean.BluetoothDeviceInfo;
import org.itzheng.and.ble.callback.BleScanCallback;
import org.itzheng.and.ble.filter.BleScanFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Title:蓝牙扫描工具类<br>
 * Description: <br>
 * 使用注意，在界面销毁时，需要手动停止扫描，否则会造成内存泄漏
 *
 * @email ItZheng@ZoHo.com
 * Created by itzheng on 2018-1-19.
 */
public class BleScanUtils {
    /**
     * 创建一个实例，每个项目里面单独
     *
     * @return
     */
    public static BleScanUtils newInstance() {
        return new BleScanUtils();
    }

    {
        //初始化 监听，如果直接赋值的话，低版本找不到新版api会报错
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanCallback_v21 = getScanCallback_v21();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            scanCallback_v18 = getScanCallback_v18();
        }
    }

    private BleScanFilter mScanFilter;

    /**
     * 添加过滤器，如果不需要过滤器可以直接添加null
     *
     * @param filter
     * @return
     */
    public BleScanUtils setFilter(BleScanFilter filter) {
        mScanFilter = filter;
        return this;
    }

    /**
     * 版本大于18的监听
     */
    @SuppressLint("NewApi")
    private BluetoothAdapter.LeScanCallback scanCallback_v18;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothAdapter.LeScanCallback getScanCallback_v18() {
        return new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (mScanFilter != null && !mScanFilter.isAdd(device, rssi, scanRecord)) {
                    //不添加，直接返回
                    return;
                }
                //应该加个过滤器
                BluetoothDeviceInfo info = new BluetoothDeviceInfo();
                info.device = device;
                info.rssi = rssi;
                info.scanRecord = scanRecord;
                addList(deviceInfoList, info);
                if (mScanCallback != null) {
                    mScanCallback.onLeScan(device, rssi, scanRecord);
                    mScanCallback.onLeScan(deviceInfoList);
                }
            }
        };
    }

    /**
     * 版本大于21的监听
     */
    private ScanCallback scanCallback_v21;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public ScanCallback getScanCallback_v21() {
        return new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if (Build.VERSION.SDK_INT >= 21) {
                    //直接调用低版本的回调，兼容低版本
                    scanCallback_v18.onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
    }

    /**
     * 根据算法，将单个蓝牙信息添加到集合中
     *
     * @param deviceInfoList
     * @param info
     */
    private void addList(List<BluetoothDeviceInfo> deviceInfoList, BluetoothDeviceInfo info) {
        BluetoothDevice newDevice = info.device;
        for (BluetoothDeviceInfo oldDeviceInfo : deviceInfoList) {
            BluetoothDevice oldDevice = oldDeviceInfo.device;
            if (oldDevice.getAddress().equalsIgnoreCase(newDevice.getAddress())) {
                oldDeviceInfo.device = info.device;
                oldDeviceInfo.rssi = info.rssi;
                oldDeviceInfo.scanRecord = info.scanRecord;
                //替换完成则返回
                return;
            }
        }
        //如果没有找到相同的地址，说明是新的，直接新增到列表前面
        deviceInfoList.add(0, info);
    }

    /**
     * 搜索到的列表集合
     */
    private final List<BluetoothDeviceInfo> deviceInfoList = new ArrayList<>();
    /**
     * 回调监听
     */
    private BleScanCallback mScanCallback;

    /**
     * 开始进行蓝牙扫描
     *
     * @param scanCallback 设置扫描回调
     */
    public void startLeScan(final BleScanCallback scanCallback) {
        setScanCallback(scanCallback);
        startLeScan();
    }

    /**
     * 开始进行蓝牙扫描
     */
    public void startLeScan() {
        deviceInfoList.clear();
        if (BluetoothUtils.hasBluetoothAdapter()) {
            BluetoothAdapter bluetoothAdapter = BluetoothUtils.getBluetoothAdapter();
            if (bluetoothAdapter != null) {
                if (Build.VERSION.SDK_INT >= 21) {
                    bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback_v21);
                } else if (Build.VERSION.SDK_INT >= 18) {
                    bluetoothAdapter.startLeScan(scanCallback_v18);
                } else {
                    //18以下的
                }
            }
        }

    }

    /**
     * 设置扫描回调
     *
     * @param scanCallback
     */
    public void setScanCallback(final BleScanCallback scanCallback) {
        mScanCallback = scanCallback;
    }

    /**
     * 停止扫描
     */
    public void stopLeScan() {
        if (BluetoothUtils.hasBluetoothAdapter()) {
            BluetoothAdapter bluetoothAdapter = BluetoothUtils.getBluetoothAdapter();
            if (Build.VERSION.SDK_INT >= 21) {
                if (bluetoothAdapter.getBluetoothLeScanner() == null) {
                    //如果扫描器为空，则不用停止，否则空指针
                    return;
                }
                bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback_v21);
            } else if (Build.VERSION.SDK_INT >= 18) {
                bluetoothAdapter.stopLeScan(scanCallback_v18);
            } else {
                //18以下的
            }
        }
    }

    /**
     * 进行内存回收
     */
    public void recycle() {
        mScanCallback = null;
//        scanCallback_v21 = null;
//        scanCallback_v18 = null;
        deviceInfoList.clear();

    }
}
