package com.tologon.android.wifilocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.List;

public class WifiInformation extends AppCompatActivity {
    private Handler mHandler;
    private TextView currentWiFiData, wifiListData;
    private Context context = this;
    private ConnectivityManager cm;
    private WifiManager wifiManager;
    private WifiReceiver wifiReceiver;
    private final String NO_CONNECTION = "No Wi-Fi connection";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_information);
        currentWiFiData = (TextView) findViewById(R.id.CurrentWiFiData);
        wifiListData = (TextView) findViewById(R.id.WiFiListData);
        setNetworkStatus();

        mHandler = new Handler();
        startRepeatingTask();
    }

    public String getWiFiNetworkInfo() {
        if(!networkStatus()) {
            return NO_CONNECTION;
        }

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

    public String getWiFiList() {
        if(!networkStatus()) {
            return NO_CONNECTION;
        }

        return wifiReceiver.getResults();
    }

    @Override
    public  void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
        unregisterReceiver(wifiReceiver);
    }

    Runnable mStatusChecker = new Runnable() {
        int mInterval = 10; // 1 millisecond = 1,000 value

        @Override
        public void run() {
            try {
                String currentNetwork = getWiFiNetworkInfo();
                currentWiFiData.setText(currentNetwork);

                if(wifiReceiver == null) {
                    wifiReceiver = new WifiReceiver();
                }

                registerReceiver(wifiReceiver, new IntentFilter(
                        WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
                ));
                wifiManager.startScan();
                String nearbyNetworks = getWiFiList();
                wifiListData.setText(nearbyNetworks);
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
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    class WifiReceiver extends BroadcastReceiver {
        String results;

        public void onReceive(Context c, Intent i) {
            List<ScanResult> scanList = wifiManager.getScanResults();
            StringBuilder sb = new StringBuilder();
            for(ScanResult item : scanList) {
                sb.append(item.toString() + "\n");
            }

            results = sb.toString();
        }

        public String getResults() {
            return results;
        }
    }
}
