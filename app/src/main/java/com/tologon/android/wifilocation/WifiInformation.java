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
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private final String NO_LOCATION_IDENTIFICATION = "Couldn't identify the location.";
    private final String CS_DEPARTMENT_BACK = "18:64:72:f3:41:72";
    private final String CS_DEPARTMENT_CENTER = "18:64:72:f3:30:f2";
    private final String CS_DEPARTMENT_FRONT = "18:64:72:f3:40:52";
    private int fieldImgXY[] = new int[2];
    private final String LOG_TAG = "LOCATION";
    private MyImageView mContentView;
    private Random rng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        rng = new Random();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_information);
        currentWiFiData = (TextView) findViewById(R.id.CurrentWiFiData);
        wifiListData = (ListView) findViewById(R.id.WiFiListData);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, networks);
        wifiListData.setAdapter(adapter);
        setNetworkStatus();
        mContentView = (MyImageView) findViewById(R.id.imageView);

        requestPermission();
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
        ));

        mHandler = new Handler();
        startRepeatingTask();
    }

    public String getWITLocation() {
        if(!networkStatus()) {
            return NO_CONNECTION;
        }
        String closestAP = closestAP();

        if(closestAP == null) {
            return NO_LOCATION_IDENTIFICATION;
        }
        else if(closestAP.equals(CS_DEPARTMENT_BACK)) {
            return "You are in the back of the CS department.";
        }
        else if(closestAP.equals(CS_DEPARTMENT_CENTER)) {
            return "You are in the center of the CS department.";
        }
        else if(closestAP.equals(CS_DEPARTMENT_FRONT)) {
            return "You are in the front of the CS department.";
        }
        else {
            return NO_LOCATION_IDENTIFICATION;
        }
    }

    private String closestAP() {
        String closestAP = null;
        int bestRSSI = Integer.MIN_VALUE;

        List<ScanResult> scanList = wifiManager.getScanResults();
        for(ScanResult item : scanList) {
            if(item.SSID.equals(SCHOOL_NETWORK_1) || item.SSID.equals(SCHOOL_NETWORK_2)) {
                if(item.level > bestRSSI) {
                    bestRSSI = item.level;
                    closestAP = item.BSSID;
                }
            }
        }

        return closestAP;
    }

//    public String getWiFiNetworkInfo() {
//        if(!networkStatus()) {
//            return NO_CONNECTION;
//        }
//
//        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        StringBuilder sb = new StringBuilder();
//        String SSID = wifiInfo.getSSID().replace('\"', ' ');
//        int RSSI = wifiInfo.getRssi();
//        AP_MAC = wifiInfo.getBSSID();
//        double frequency = wifiInfo.getFrequency() * 1.0 / 1000;
//        int speed = wifiInfo.getLinkSpeed();
//
//        sb.append("SSID:" + SSID + "\n");
//        sb.append("RSSI: " + RSSI + "\n");
//        sb.append("AP MAC: " + AP_MAC + "\n");
//        sb.append("Frequency: " + frequency + " GHz\n");
//        sb.append("Speed: " + speed + " Mbps\n");
//
//        return sb.toString();
//    }

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
        int mInterval = 5000; // 1 millisecond = 1,000 value
        int minRadius = 100;
        int maxRadius = 500; // its actually maxRadius - minRadius
        int userX, userY;

        @Override
        public void run() {
            try {
                //String currentNetwork = getWiFiNetworkInfo();
//                String currentLocation = getWITLocation();
//                currentWiFiData.setText(currentLocation);
//                wifiManager.startScan();
//                registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                randomlySetRadiuses();
                findUser();

                mContentView.setUser(userX, userY);
                String userPosition = "USER POS: " + xyString(userX, userY);
                //Toast.makeText(context, userPosition, Toast.LENGTH_SHORT).show();
                mContentView.invalidate();
            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }

        private void randomlySetRadiuses() {
            int r1 = rng.nextInt(maxRadius) + minRadius;
            int r2 = rng.nextInt(maxRadius) + minRadius;
            int r3 = rng.nextInt(maxRadius) + minRadius;
            mContentView.setWifiRadius(1, r1);
            mContentView.setWifiRadius(2, r2);
            mContentView.setWifiRadius(3, r3);
        }

        private void findUser() {
            Circle[] circles = populateCircles();

            int top = 0;
            int bot = 0;

            for (int i=0; i<3; i++) {
                Circle c = circles[i];
                Circle c2, c3;
                if (i==0) {
                    c2 = circles[1];
                    c3 = circles[2];
                }
                else if (i==1) {
                    c2 = circles[0];
                    c3 = circles[2];
                }
                else {
                    c2 = circles[0];
                    c3 = circles[1];
                }

                int d = c2.x - c3.x;

                int v1 = (c.x * c.x + c.y * c.y) - (c.r * c.r);
                top += d*v1;

                int v2 = c.y * d;
                bot += v2;

            }

            int y = top / (2*bot);
            Circle c1 = circles[0];
            Circle c2 = circles[1];
            top = c2.r*c2.r+c1.x*c1.x+c1.y*c1.y-c1.r*c1.r-c2.x*c2.x-c2.y*c2.y-2*(c1.y-c2.y)*y;
            bot = c1.x-c2.x;
            int x = top / (2*bot);

            userX = x;
            userY = y;
        }

        private Circle[] populateCircles() {
            Circle c1 = new Circle();
            c1.x = mContentView.getBackX();
            c1.y = mContentView.getBackY();
            c1.r = mContentView.getBackRadius();
            Circle c2 = new Circle();
            c2.x = mContentView.getCenterX();
            c2.y = mContentView.getCenterY();
            c2.r = mContentView.getCenterRadius();
            Circle c3 = new Circle();
            c3.x = mContentView.getFrontX();
            c3.y = mContentView.getFrontY();
            c3.r = mContentView.getFrontRadius();
            Circle[] circles = {c2, c3, c1};
            return circles;
        }
    };

    private class Circle {
        int x, y, r;
    }

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
            if(!networkStatus()) {
                return;
            }

            /*
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
            */
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Use onWindowFocusChanged to get the placement of
        // the image because we have to wait until the image
        // has actually been placed on the screen  before we
        // get the coordinates. That makes it impossible to
        // do in onCreate, that would just give us (0, 0).
        mContentView.getLocationOnScreen(fieldImgXY);

        String locationInfo = "fieldImage location on screen: " + xyString(fieldImgXY[0], fieldImgXY[1]);
        Log.i(LOG_TAG, locationInfo);
        Toast.makeText(context, locationInfo, Toast.LENGTH_SHORT).show();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.i(LOG_TAG, "touch event - down");

            int eventX = (int) event.getX();
            int eventY = (int) event.getY();
            Log.i(LOG_TAG, "event (x, y) = " + xyString(eventX, eventY));

            int xOnField = eventX - fieldImgXY[0];
            int yOnField = eventY - fieldImgXY[1];
            String locationInfo = "on field (x, y) = " + xyString(xOnField, yOnField);
            Log.i(LOG_TAG, locationInfo);
            Toast.makeText(context, locationInfo, Toast.LENGTH_SHORT).show();
        }
        return super.onTouchEvent(event);
    }

    private String xyString(int x, int y) {
        return "(" + x + ", " + y + ")";
    }
}
