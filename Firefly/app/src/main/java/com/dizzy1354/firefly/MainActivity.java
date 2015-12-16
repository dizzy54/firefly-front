package com.dizzy1354.firefly;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dizzy1354.firefly.helpers.UidValidator;
import com.dizzy1354.firefly.helpers.Utils;
import com.dizzy1354.firefly.models.AdRecord;
import com.dizzy1354.firefly.models.Beacon;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.Eddystone;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneTLM;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneUID;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneURL;
import com.neovisionaries.bluetooth.ble.advertising.ServiceData;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    String TAG = "MainActivity";

    BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    int mCurrentapiVersion = android.os.Build.VERSION.SDK_INT;
    int REQUEST_ENABLE_BT = 1;
    private ArrayList<BluetoothDevice> mBleDeviceList = null;
    //private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothLeScanner mLeScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private HashMap<String, Beacon> mBeacons =  new HashMap<String, Beacon>();

    // The Eddystone Service UUID, 0xFEAA.
    private static final ParcelUuid EDDYSTONE_SERVICE_UUID =
            ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");

    // Scan parameters
    private static final int BLE_SCAN_DURATION = 4000;
    private static final int BLE_SCAN_INTERVAL = 5000;

    private final MyHandler mHandler = new MyHandler(this);

    //map params and variables
    GoogleMap googleMap;
    private static final LatLng MUMBAI = new LatLng(18.9750, 72.8258);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        */

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //googleMap = mapFragment.getMap();
        //googleMap.setMyLocationEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //
        if (checkBleAvailable()) {
            Log.d(TAG, "onCreate: ble is available");
            //BluetoothManager bluetoothManager = initializeBle((Context) this);
            // mBluetoothAdapter = bluetoothManager.getAdapter();
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            setupScanSettings();
            //enableBle();

        }
        else {
            // write code to cover any views in app for unavailability of BLE
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    */

    /*
    Callback for map fragment
     */
    @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(true);
        map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
        // Move the camera instantly to Sydney with a zoom of 15.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(MUMBAI, 10));

        /*
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        */

        Location myLocation = Utils.getMyLocation(this);
        if (myLocation!=null) {
            LatLng myLocationLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            // Construct a CameraPosition focusing on current location and animate the camera to that position.
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(myLocationLatLng)      // Sets the center of the map to current location
                    .zoom(14)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            Utils.setBluetooth(true);
            // registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            mHandler.post(scanTaskRunnable);
            //finish();
            return;
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        // start scanning
        mHandler.post(scanTaskRunnable);
    }

    public void onPause() {
        super.onPause();
        // stop listening to bluetooth
        // unregisterReceiver(mReceiver);
        // stop scanning
        mHandler.removeCallbacks(scanTaskRunnable);
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            myScanLeDevice(false);
            mBluetoothAdapter.disable();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(21)
    private void setupScanSettings(){
        // setup for lollypop
        if (mCurrentapiVersion>=21) {
            // api 21+ specific settings
            if (mLeScanner == null) {
                Log.d(TAG, "setupScanSettings: mLeScanner is null");
            }
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build();
            filters = new ArrayList<ScanFilter>();
        }
    }

    private Boolean checkBleAvailable() {
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        Boolean return_val = true;
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            return_val = false;
            //finish();
        }
        return return_val;
    }

    @TargetApi(18)
    private BluetoothManager initializeBle(Context context) {
        // Initializes Bluetooth adapter. Returns manager
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(context.BLUETOOTH_SERVICE);
        return bluetoothManager;

    }

    @TargetApi(18)
    private void myScanLeDevice(final boolean enable){
        if (mCurrentapiVersion >= 21) {
            Log.d(TAG, "myScanLeDevice: api >= 21");
            myScanLeDeviceNew(enable);
        }
        else {
            Log.d(TAG, "myScanLeDevice: api < 21");
            myScanLeDeviceOld(enable);
        }
    }

    @TargetApi(21)
    private void myScanLeDeviceNew(final boolean enable){
        if (enable & mBluetoothAdapter.isEnabled()) {
            if (mLeScanner!=null) {
                mScanning = true;
                mLeScanner.startScan(filters, settings, mScanCallback);
                Log.d(TAG, "starting scan api >= 21");
            }
            else {
                mLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
        }
        else{
            if (mLeScanner!=null & mBluetoothAdapter.isEnabled()) {
                mScanning = false;
                mLeScanner.stopScan(mScanCallback);
                Log.d(TAG, "stopping scan api >= 21");
            }
            else {
                mLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
        }
    }

    @TargetApi(18)
    private void myScanLeDeviceOld(final boolean enable){
        if (enable) {
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
        else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback for api level 18 to 20
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
        new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi,
                                 final byte[] scanRecord) {
                processResult(device, rssi, scanRecord);
            }
        };

    // Device scan callback for api level >= 21
    @SuppressLint("NewApi")
    private ScanCallback mScanCallback = (mCurrentapiVersion>=21) ? (new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            /*  Connect to  device  found   */
            Log.i("callbackType", String.valueOf(callbackType));

            BluetoothDevice btDevice = result.getDevice();
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            /*  Process a   batch   scan    results */
            for (ScanResult sr : results) {
                processResult(sr);
            }
        }
    }) : (null);

    private Runnable scanTaskRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "scanTaskRunnable started");
            // Clear device array
            mBeacons.clear();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    myScanLeDevice(false);
                }
            }, BLE_SCAN_DURATION);
            myScanLeDevice(true);
            mHandler.postDelayed(scanTaskRunnable, BLE_SCAN_INTERVAL);
        }
    };

    @TargetApi(21)
    public void processResult(ScanResult result){
        ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord == null) {
            return;
        }

        String deviceAddress = result.getDevice().getAddress();
        Beacon beacon;
        if (!mBeacons.containsKey(deviceAddress)) {
            beacon = new Beacon(deviceAddress, result.getRssi());
            mBeacons.put(deviceAddress, beacon);
        } else {
            beacon = mBeacons.get(deviceAddress);
            beacon.lastSeenTimestamp = System.currentTimeMillis();
            beacon.rssi = result.getRssi();

        }

        byte[] serviceData = scanRecord.getServiceData(EDDYSTONE_SERVICE_UUID);
        validateServiceData(deviceAddress, serviceData);

        mHandler.sendMessage(Message.obtain(null, 0, beacon));

    }

    @TargetApi(18)
    public void processResult(final BluetoothDevice device, int rssi,
                              final byte[] scanRecord){
        //ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord == null) {
            return;
        }

        // Parse the payload of the advertisement packet.
        List<ADStructure> structures =
                ADPayloadParser.getInstance().parse(scanRecord);

        // For each AD structure contained in the advertising packet.
        for (ADStructure structure : structures)
        {
            // If the ADStructure instance can be cast to Eddystone.
            if (structure instanceof Eddystone)
            {
                String deviceAddress = device.getAddress();
                Beacon beacon;
                if (!mBeacons.containsKey(deviceAddress)) {
                    beacon = new Beacon(deviceAddress, rssi);
                    mBeacons.put(deviceAddress, beacon);
                } else {
                    beacon = mBeacons.get(deviceAddress);
                    beacon.lastSeenTimestamp = System.currentTimeMillis();
                    beacon.rssi = rssi;
                }


                byte[] data = structure.getData();

                byte[] serviceData = new byte[data.length-2];
                System.arraycopy(data, 2, serviceData, 0, data.length-2);
                validateServiceData(deviceAddress, serviceData);
                mHandler.sendMessage(Message.obtain(null, 0, beacon));
            }
        }
    }
    // Checks the frame type and hands off the service data to the validation module.
    private void validateServiceData(String deviceAddress, byte[] serviceData) {
        Beacon beacon = mBeacons.get(deviceAddress);
        if (serviceData == null) {
            String err = "Null Eddystone service data";
            beacon.frameStatus.nullServiceData = err;
            logDeviceError(deviceAddress, err);
            return;
        }
        Log.v(TAG, deviceAddress + " " + Utils.toHexString(serviceData));
        switch (serviceData[0]) {
            case Constants.UID_FRAME_TYPE:
                Log.d(TAG, "validateServiceData: UID frame type");
                UidValidator.validate(deviceAddress, serviceData, beacon);
                Log.d(TAG, "UID = " + beacon.uidStatus.uidValue);
                //Log.d(TAG, "validateServiceData: Service data = "+Utils.toHexString(serviceData));
                break;
            case Constants.TLM_FRAME_TYPE:
                Log.d(TAG, "validateServiceData: TLM frame type");
                //TlmValidator.validate(deviceAddress, serviceData, beacon);
                break;
            case Constants.URL_FRAME_TYPE:
                Log.d(TAG, "validateServiceData: URL frame type");
                //UrlValidator.validate(deviceAddress, serviceData, beacon);
                break;
            default:
                String err = String.format("Invalid frame type byte %02X", serviceData[0]);
                beacon.frameStatus.invalidFrameType = err;
                logDeviceError(deviceAddress, err);
                break;
        }
    }

    private static class MyHandler extends Handler {

        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            Beacon beacon = (Beacon) msg.obj;
            activity.mBeacons.put(beacon.deviceAddress, beacon);
            TextView bleIndicatorView = (TextView) activity.findViewById(R.id.ble_indicator);
            String displayString = activity.mBeacons.size() + " beacon(s) nearby";
            bleIndicatorView.setText(displayString);
        }
    }

    private void logDeviceError(String deviceAddress, String err) {
        Log.e(TAG, deviceAddress + ": " + err);
    }
}
