package com.tologon.android.wifilocation;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class WifiInformation extends AppCompatActivity {
    private Handler mHandler;
    private TextView currentWiFiData, wifiListData;
    private Context context = this;
    private ConnectivityManager cm;
    private WifiManager wifiManager;
    private WifiReceiver wifiReceiver;
    private final String NO_CONNECTION = "No Wi-Fi connection";
    private final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_information);
        currentWiFiData = (TextView) findViewById(R.id.CurrentWiFiData);
        wifiListData = (TextView) findViewById(R.id.WiFiListData);
        setNetworkStatus();

        requestPermission();
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
        ));
        wifiManager.startScan();
        Toast.makeText(context, "Starting WiFi scan..", Toast.LENGTH_LONG).show();

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
    public void onDestroy() {
        stopRepeatingTask();
        unregisterReceiver(wifiReceiver);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        unregisterReceiver(wifiReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            registerReceiver(wifiReceiver, new IntentFilter(
                    WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
            ));
            wifiManager.startScan();
        }
    }

    Runnable mStatusChecker = new Runnable() {
        int mInterval = 5000; // 1 millisecond = 1,000 value

        @Override
        public void run() {
            try {
                String currentNetwork = getWiFiNetworkInfo();
                currentWiFiData.setText(currentNetwork);
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
            String scanListLength = "# of WiFi networks: " + String.valueOf(scanList.size());
            Toast.makeText(context, scanListLength, Toast.LENGTH_LONG).show();
            StringBuilder sb = new StringBuilder();
            for(ScanResult item : scanList) {
                sb.append(item.toString() + "\n");
            }

            results = sb.toString();
            wifiListData.setText(results);
        }

        public String getResults() {
            return results;
        }
    }

    public void requestPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }else{
            wifiManager.startScan();
        }
    }
}
