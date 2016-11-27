package com.tologon.android.wifilocation;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class WifiInformation extends AppCompatActivity {
    private Handler mHandler;
    TextView WiFiView;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_information);
        WiFiView = (TextView) findViewById(R.id.WiFiView);

        mHandler = new Handler();
        startRepeatingTask();
    }

    public String getSignalStrength() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return String.valueOf(wifiInfo.getRssi());
    }

    @Override
    public  void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        int mInterval = 10; // 1 millisecond = 1,000 value

        @Override
        public void run() {
            try {
                String RSSI = getSignalStrength();
                WiFiView.setText(RSSI);
            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }
}
