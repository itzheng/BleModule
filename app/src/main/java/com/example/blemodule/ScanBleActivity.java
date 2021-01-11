package com.example.blemodule;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.itzheng.and.ble.bean.BluetoothDeviceInfo;
import org.itzheng.and.ble.callback.BleScanCallback;
import org.itzheng.and.ble.filter.BleScanFilter;
import org.itzheng.and.ble.utils.BleScanUtils;
import org.itzheng.and.ble.utils.ByteUtils;
import org.w3c.dom.Text;

import java.util.List;

/**
 * Title:搜索蓝牙,列表显示，点击可以连接蓝牙<br>
 * Description: <br>
 *
 * @email ItZheng@ZoHo.com
 * Created by itzheng on 2020-9-15.
 */
public class ScanBleActivity extends AppCompatActivity {
    BleScanUtils bleScanUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("搜索蓝牙");
        setContentView(R.layout.activity_scan_ble);
        initView();
        initUtils();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleScanUtils != null) {
            bleScanUtils.recycle();
            bleScanUtils = null;

        }
    }

    private static final String TAG = "ScanBleActivity";

    private void initUtils() {
        if (bleScanUtils == null) {
            bleScanUtils = BleScanUtils.newInstance();
            bleScanUtils.setFilter(new BleScanFilter() {
                @Override
                public boolean isAdd(BluetoothDevice device, int rssi, byte[] scanRecord) {

                    Log.w(TAG, "isAdd: " + device.getName() + " " + ByteUtils.toHexString(scanRecord));
                    return true;
                }
            });
            bleScanUtils.setScanCallback(new BleScanCallback() {
                @Override
                public void onLeScan(List<BluetoothDeviceInfo> deviceInfoList) {
                    Log.w(TAG, "onLeScan: " + deviceInfoList.size());
                    setAdapter(deviceInfoList);
                }

                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

                }
            });
        }
    }

    ShowBleAdapter adapter;

    /**
     * 将搜索到的蓝牙显示到界面
     *
     * @param deviceInfoList
     */
    private void setAdapter(List<BluetoothDeviceInfo> deviceInfoList) {
        if (adapter == null) {
            adapter = new ShowBleAdapter();
            listView.setAdapter(adapter);
        }
        adapter.submitItems(deviceInfoList);
        adapter.notifyDataSetChanged();

    }

    //    android.support.v4.widget.SwipeRefreshLayout refreshLayout;
    ListView listView;
    Button btnStatus;
    ProgressBar progressBar;

    private void initView() {
//        refreshLayout = findViewById(R.id.refreshLayout);
        listView = findViewById(R.id.listView);
        btnStatus = findViewById(R.id.btnStatus);
        progressBar = findViewById(R.id.progressBar);
        btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScanning) {
                    stopScanBle();
                } else {
                    startScanBle();
                }

            }
        });
    }

    boolean isScanning = false;

    /**
     * 开始扫描
     */
    private void startScanBle() {
        requestLocationPermission();
        isScanning = true;
        progressBar.setVisibility(View.VISIBLE);
        btnStatus.setText("停止扫描");
        bleScanUtils.startLeScan();
    }

    /**
     * 停止扫描
     */
    private void stopScanBle() {
        isScanning = false;
        progressBar.setVisibility(View.GONE);
        btnStatus.setText("开始扫描");
        bleScanUtils.stopLeScan();
    }

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        0);
            }
        }
    }

    private class ShowBleAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mItems == null ? 0 : mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final BluetoothDeviceInfo deviceInfo = (BluetoothDeviceInfo) getItem(position);
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_view_show_ble, parent, false);
            TextView tvName = view.findViewById(R.id.tvName);
            TextView tvAddress = view.findViewById(R.id.tvAddress);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnBleActivity.startActivity(v.getContext(), deviceInfo.device);
                    stopScanBle();
                }
            });
            tvName.setText(toString(deviceInfo.device.getName()));
            tvAddress.setText(toString(deviceInfo.device.getAddress()));
            return view;
        }

        private String toString(String name) {
            return name + "";
        }

        private List<BluetoothDeviceInfo> mItems;

        public void submitItems(List<BluetoothDeviceInfo> items) {
            mItems = items;
        }
    }
}
