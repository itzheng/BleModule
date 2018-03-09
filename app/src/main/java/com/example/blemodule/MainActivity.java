package com.example.blemodule;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.itzheng.and.ble.utils.BleScanUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final BleScanUtils bleScanUtils = BleScanUtils.newInstance();
    }
}
