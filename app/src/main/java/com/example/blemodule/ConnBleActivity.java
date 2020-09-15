package com.example.blemodule;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.itzheng.and.ble.callback.OnConnectionStateChangeListener;
import org.itzheng.and.ble.callback.OnReceiveDataListener;
import org.itzheng.and.ble.utils.BleOptionUtils;
import org.itzheng.and.ble.utils.ByteUtils;

/**
 * Title:蓝牙连接，提示连接成功，连接失败，发送消息，接收消息。返回则自动断开连接。<br>
 * Description: <br>
 *
 * @email ItZheng@ZoHo.com
 * Created by itzheng on 2020-9-15.
 */
public class ConnBleActivity extends AppCompatActivity {
    private static final String EXTRA_BLUETOOTH_DEVICE = "EXTRA_BLUETOOTH_DEVICE";

    public static void startActivity(Context context, BluetoothDevice device) {
        Intent intent = new Intent(context, ConnBleActivity.class);
        intent.putExtra(EXTRA_BLUETOOTH_DEVICE, device);
        context.startActivity(intent);
    }

    private static final String TAG = "ConnBleActivity";
    BluetoothDevice bluetoothDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("连接蓝牙");
        bluetoothDevice = getIntent().getParcelableExtra(EXTRA_BLUETOOTH_DEVICE);
        if (bluetoothDevice == null) {
            Log.w(TAG, "onCreate: bluetoothDevice == null");
            finish();
            return;
        }
        Log.w(TAG, "onCreate: " + bluetoothDevice.getName() + "  " + bluetoothDevice.getAddress());
        setContentView(R.layout.activity_conn);
        initView();
        initUtils();
    }

    BleOptionUtils bleOptionUtils;

    private void initUtils() {
        if (bleOptionUtils == null) {
            bleOptionUtils = BleOptionUtils.newInstance(this);
            bleOptionUtils.addOnConnectionStateChangeListener(new OnConnectionStateChangeListener() {
                @Override
                public void onConnected() {
                    isConn = true;
                    updateConnStatus();
                    dismissLoading();
                }

                @Override
                public void onDisconnected() {
                    isConn = false;
                    updateConnStatus();
                }

                @Override
                public void onServicesDiscovered() {

                }
            });
            bleOptionUtils.addOnReceiveDataListener(new OnReceiveDataListener() {
                @Override
                public void onReceiveData(byte[] value) {
                    sbReceive.append(ByteUtils.toHexString(value));
                    tvText.post(new Runnable() {
                        @Override
                        public void run() {
                            tvText.setText(sbReceive.toString());
                        }
                    });

                }
            });
        }
    }


    private void updateConnStatus() {
        if (isConn) {
            btnStatus.setText("断开连接");
        } else {
            btnStatus.setText("开始连接");
        }
    }

    Button btnStatus;
    ProgressBar progressBar;
    Button btnClear;
    TextView tvText;
    EditText etSend;
    Button btnSend;
    boolean isConn = false;
    StringBuffer sbReceive = new StringBuffer();

    private void initView() {
        btnStatus = findViewById(R.id.btnStatus);
        btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConn) {
                    disConn();
                } else {
                    connBle();
                }
            }
        });
        progressBar = findViewById(R.id.progressBar);
        btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //清空收到的数据
                sbReceive.setLength(0);
                tvText.setText(sbReceive.toString());
            }
        });
        tvText = findViewById(R.id.tvText);
        etSend = findViewById(R.id.etSend);
        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送数据
                postData();
            }
        });
    }

    /**
     * 发送数据
     */
    private void postData() {
        if (!isConn) {
            return;
        }
        bleOptionUtils.post(etSend.getText().toString().getBytes());
    }

    /**
     * 连接蓝牙
     */
    private void connBle() {
        showLoading();
        bleOptionUtils.connect(bluetoothDevice.getAddress());
    }

    private void showLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });

    }

    private void dismissLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    /**
     * 断开连接
     */
    private void disConn() {
        bleOptionUtils.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleOptionUtils != null) {
            if (isConn) {
                bleOptionUtils.disconnect();
            }
            bleOptionUtils.recycle();
            bleOptionUtils = null;
        }
    }
}
