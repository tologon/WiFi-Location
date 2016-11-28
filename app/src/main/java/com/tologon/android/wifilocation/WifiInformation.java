package com.tologon.android.wifilocation;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    ConnectivityManager cm;
    NetworkInfo activeNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_information);
        WiFiView = (TextView) findViewById(R.id.WiFiView);
        setNetworkStatus();

        mHandler = new Handler();
        startRepeatingTask();
    }

    public String getWiFiNetworkInfo() {
        if(!networkStatus()) {
            return "No Wi-Fi connection";
        }

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        StringBuilder sb = new StringBuilder();
        String SSID = wifiInfo.getSSID().replace('\"', ' ');
        int RSSI = wifiInfo.getRssi();
        String AP_MAC = wifiInfo.getBSSID();
        double frequency = wifiInfo.getFrequency() * 1.0 / 1000;
        int speed = wifiInfo.getLinkSpeed();

        sb.append("SSID:" + SSID + "\n");
        sb.append("RSSI: " + RSSI + "\n");
        sb.append("AP MAC: " + AP_MAC + "\n");
        sb.append("Frequency: " + frequency + " GHz\n");
        sb.append("Speed: " + speed + " Mbps\n");

        return sb.toString();
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
                String info = getWiFiNetworkInfo();
                WiFiView.setText(info);
            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private void startRepeatingTask() {
        mStatusChecker.run();
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    private void setNetworkStatus() {
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private boolean networkStatus() {
        activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
