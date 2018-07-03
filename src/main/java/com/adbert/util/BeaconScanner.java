package com.adbert.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.adbert.util.data.BeaconData;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chihhan on 2017/9/15.
 */

@SuppressLint("MissingPermission")
public class BeaconScanner {

    private Map<String, BeaconData> map = new HashMap<>();
    private List<String> uuids = new ArrayList<>();

    public BeaconScanner(List<String> uuids) {
        this.uuids = uuids;
    }

    BluetoothAdapter bluetoothAdapter;
    private boolean isScanning = false;

    public void startScan() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            SDKUtil.logTestMsg("BeaconScanner bluetoothAdapter == null");
        } else {
            if (bluetoothAdapter.isEnabled()) {
                isScanning = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    bluetoothAdapter.getBluetoothLeScanner().startScan(callback);
                } else {
                    bluetoothAdapter.startLeScan(leScanCallback);
                }
            } else {
                SDKUtil.logTestMsg("BeaconScanner onNotEnable");
            }
        }
    }

//    private ScanSettings getSetting() {
////        ScanSettings.Builder builder = new ScanSettings.Builder();
////        builder.setReportDelay(0);
////        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////            builder.setCallbackType(ScanSettings.CALLBACK_TYPE_MATCH_LOST);//ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
////            builder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
////            builder.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);
////        }
////        return builder.build();
//
//        return new ScanSettings.Builder()
//                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                .build();
//    }
//
//    private List<ScanFilter> getMatchList() {
//        List<ScanFilter> filters = new ArrayList<>();
//        for (String u : uuids) {
//            UUID uuid = UUID.fromString(u);
//            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(uuid)).build();
//            filters.add(filter);
//        }
//        return filters;
//    }

    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            parseScanResult(scanRecord);
        }
    };

    ScanCallback callback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            //TODO:有的手機可能只回傳一次
            if (!result.getDevice().fetchUuidsWithSdp()) {
                SDKUtil.logTestMsg("BeaconScanner onNoResult");
            }

            byte[] scanRecord = result.getScanRecord().getBytes();
            parseScanResult(scanRecord);

//            int rssi = result.getRssi();
//            BluetoothDevice device = result.getDevice();
        }


        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            SDKUtil.logTestMsg("BeaconScanner onBatchScanResults");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            SDKUtil.logTestMsg("BeaconScanner onScanFailed " + errorCode);
        }

    };


    private void parseScanResult(byte[] scanRecord) {
        String uuid = IntToHex2(scanRecord[9] & 0xff) + IntToHex2(scanRecord[10] & 0xff)
                + IntToHex2(scanRecord[11] & 0xff) + IntToHex2(scanRecord[12] & 0xff)
                + "-" + IntToHex2(scanRecord[13] & 0xff) + IntToHex2(scanRecord[14] & 0xff)
                + "-" + IntToHex2(scanRecord[15] & 0xff) + IntToHex2(scanRecord[16] & 0xff)
                + "-" + IntToHex2(scanRecord[17] & 0xff) + IntToHex2(scanRecord[18] & 0xff)
                + "-" + IntToHex2(scanRecord[19] & 0xff) + IntToHex2(scanRecord[20] & 0xff)
                + IntToHex2(scanRecord[21] & 0xff) + IntToHex2(scanRecord[22] & 0xff)
                + IntToHex2(scanRecord[23] & 0xff) + IntToHex2(scanRecord[24] & 0xff);

        String major = String.valueOf((scanRecord[25] & 0xff) * 0x100 + (scanRecord[26] & 0xff));
        String minor = String.valueOf((scanRecord[27] & 0xff) * 0x100 + (scanRecord[28] & 0xff));
        uuid = uuid.toLowerCase();
        SDKUtil.logTestMsg("BeaconScanner " + uuid);
        if (isMatch(uuid)) {
            String key = uuid + major + minor;
            BeaconData beaconData;
            if (map.get(key) != null) {
                beaconData = map.get(key);
                beaconData.updateTime();
            } else {
                beaconData = new BeaconData();
                beaconData.setUuid(uuid);
                beaconData.setMajor(major);
                beaconData.setMinor(minor);
            }
            map.put(key, beaconData);
        }
    }

    private String IntToHex2(int i) {
        char hex_2[] = {Character.forDigit((i >> 4) & 0x0f, 16), Character.forDigit(i & 0x0f, 16)};
        String hex_2_str = new String(hex_2);
        return hex_2_str.toUpperCase();

    }

    private boolean isMatch(String uuid) {
        for (String u : uuids) {
            if (u.toLowerCase().equals(uuid)) {
                return true;
            }
        }
        return false;
    }


    public void stopScan() {
        if (isScanning) {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    bluetoothAdapter.getBluetoothLeScanner().stopScan(callback);
                } else {
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }
            isScanning = false;
        }
    }

    public String getResult(String aaid) {
        JSONArray array = new JSONArray();
        for (Map.Entry<String, BeaconData> entry : map.entrySet()) {
            BeaconData value = entry.getValue();
            try {
                array.put(value.getJSON(aaid));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String result = array.toString();
        SDKUtil.logTestMsg("BeaconScanner result = " + result);
        return result;
    }

}
