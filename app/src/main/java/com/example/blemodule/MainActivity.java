package com.example.blemodule;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.itzheng.and.ble.bean.BluetoothDeviceInfo;
import org.itzheng.and.ble.callback.BleScanCallback;
import org.itzheng.and.ble.callback.OnBleSwitchStatusChangeListener;
import org.itzheng.and.ble.callback.OnConnectionStateChangeListener;
import org.itzheng.and.ble.callback.OnReceiveDataListener;
import org.itzheng.and.ble.utils.BleOptionUtils;
import org.itzheng.and.ble.utils.BleScanUtils;
import org.itzheng.and.ble.utils.BleSwitchStatusUtils;
import org.itzheng.and.ble.utils.ByteUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    final BleScanUtils bleScanUtils = BleScanUtils.newInstance();
    BleOptionUtils bleOptionUtils;
    BluetoothDevice mDevice;
    View flProgress;
    TextView tvOptBle;
    TextView tvBleMsg;
    TextView tvBleName;
    TextView tvBleAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (true) {
            testBleSwitch();
            return;
        }
        bleOptionUtils = BleOptionUtils.newInstance(this);
        setContentView(R.layout.activity_main);
        flProgress = findViewById(R.id.flProgress);
        dismissProgress();
        tvOptBle = findViewById(R.id.tvOptBle);
        tvBleMsg = findViewById(R.id.tvBleMsg);
        tvBleName = findViewById(R.id.tvBleName);
        tvBleAdd = findViewById(R.id.tvBleAdd);
        setConnectText();
        setBleInfo();
        tvOptBle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                if (bleOptionUtils.isConnect()) {
                    //断开连接
                    bleOptionUtils.disconnect();
                } else {
                    startScan();
                    tvOptBle.setText("正在搜索蓝牙");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleSwitchStatusUtils.recycle();
    }

    BleSwitchStatusUtils bleSwitchStatusUtils = BleSwitchStatusUtils.newInstance(this);

    private void testBleSwitch() {
        bleSwitchStatusUtils.addOnBleSwitchStatusChangeListener(new OnBleSwitchStatusChangeListener() {
            @Override
            public void onStateOn() {
                Log.d(TAG, "onStateOn: ");
            }

            @Override
            public void onStateOff() {
                Log.d(TAG, "onStateOff: ");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

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

    private void setBleInfo() {

        tvBleAdd.setText(bleOptionUtils.getCurrentAddress());
        tvBleName.setText(bleOptionUtils.getCurrentName());
    }

    private void dismissProgress() {
        flProgress.setVisibility(View.GONE);
    }

    private void showProgress() {
        flProgress.setVisibility(View.VISIBLE);
    }

    private void setConnectText() {
        if (bleOptionUtils.isConnect()) {
            //断开连接
            tvOptBle.setText("点击断开连接");
        } else {
            tvOptBle.setText("点击开始连接");
        }
    }

    int i;

    private void startScan() {
        requestLocationPermission();
        bleScanUtils.startLeScan(new BleScanCallback() {
            @Override
            public void onLeScan(List<BluetoothDeviceInfo> deviceInfoList) {

            }

            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                Log.d(TAG, "onLeScan: " + device.getName());
                String msg = ++i + " " + device.getAddress() + "\n";
                insert(tvBleMsg, msg);
                if (device.getName() != null && device.getName().startsWith("HMSoft")) {
                    bleScanUtils.stopLeScan();
                    mDevice = device;
                    starBleTest();
                }
            }
        });
    }

    public void insert(TextView textView, String str) {
        textView.setText(str + "" + textView.getText());
    }

    protected void starBleTest() {
        bleOptionUtils.connect(mDevice.getAddress());
        bleOptionUtils.addOnConnectionStateChangeListener(new OnConnectionStateChangeListener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "onConnected: ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgress();
                        setConnectText();
                        tvBleMsg.setText("");
                    }
                });

            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "onDisconnected: ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgress();
                        setConnectText();
                    }
                });
            }

            @Override
            public void onServicesDiscovered() {
                Log.d(TAG, "onServicesDiscovered: ");
            }
        });
        bleOptionUtils.addOnReceiveDataListener(new OnReceiveDataListener() {
            @Override
            public void onReceiveData(final byte[] value) {
                Log.d(TAG, "onReceiveData: " + value.length + "  " + ByteUtils.toHexString(value));
                Message message = new Message();
                message.obj = value;
                handler.sendMessage(message);
            }
        });
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            insert(tvBleMsg, ByteUtils.toHexString((byte[]) msg.obj) + "\n");
        }
    };
}
