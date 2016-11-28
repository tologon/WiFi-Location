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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WifiInformation extends AppCompatActivity {
    private Handler mHandler;
    private ListView wifiListData;
    private TextView currentWiFiData;
    private Context context = this;
    private ConnectivityManager cm;
    private WifiManager wifiManager;
    private WifiReceiver wifiReceiver;
    private final String NO_CONNECTION = "No Wi-Fi connection";
    private final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private final String SCHOOL_NETWORK_1 = "eduroam";
    private final String SCHOOL_NETWORK_2 = "LeopardGuest";
    private ArrayAdapter<String> adapter;
    private ArrayList<String> networks = new ArrayList<>();
    private String AP_MAC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_information);
        currentWiFiData = (TextView) findViewById(R.id.CurrentWiFiData);
        wifiListData = (ListView) findViewById(R.id.WiFiListData);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, networks);
        wifiListData.setAdapter(adapter);
        setNetworkStatus();

        requestPermission();
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
        ));

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
        AP_MAC = wifiInfo.getBSSID();
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
    public void onDestroy() {
        stopRepeatingTask();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
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
        int mInterval = 10; // 1 millisecond = 1,000 value

        @Override
        public void run() {
            try {
                String currentNetwork = getWiFiNetworkInfo();
                currentWiFiData.setText(currentNetwork);
                wifiManager.startScan();
                registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
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

        public void onReceive(Context c, Intent i) {
            adapter.clear();
            if(!networkStatus()) { return; }

            List<ScanResult> scanList = wifiManager.getScanResults();
            Set<String> set = new HashSet<>();

            String itemData;
            for(ScanResult item : scanList) {
                if((item.SSID.equals(SCHOOL_NETWORK_1)
                        || item.SSID.equals(SCHOOL_NETWORK_2))
                        && !item.BSSID.equals(AP_MAC)) {
                    itemData = "SSID: " + item.SSID + "\nRSSI: " + item.level + "\nMAC: " + item.BSSID;
                    set.add(itemData);
                }
            }
            adapter.addAll(set);
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
