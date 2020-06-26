package org.researchstack.backbone.omron.controller;

import android.util.AndroidRuntimeException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.neovisionaries.bluetooth.ble.advertising.ADStructure;

import org.researchstack.backbone.omron.model.entity.DiscoveredDevice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jp.co.ohq.ble.OHQDeviceManager;
import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.ble.enumerate.OHQDeviceInfoKey;
import jp.co.ohq.utility.Handler;
import jp.co.ohq.utility.Types;

public class ScanController {

    private static final String TAG = "ScanController";
    private static final long BATCHED_SCAN_INTERVAL = 1000;

    @NonNull
    private final Handler mHandler;
    @NonNull
    private final Listener mListener;
    @NonNull
    private final OHQDeviceManager mOHQDeviceManager;
    @NonNull
    private final LinkedHashMap<String, DiscoveredDevice> mDiscoveredDevices = new LinkedHashMap<>();
    @Nullable
    private OHQDeviceCategory mFilteringDeviceCategory;
    private boolean mIsScanning;
    @NonNull
    private final Runnable mBatchedScanRunnable = new Runnable() {
        @Override
        public void run() {
            _onBatchedScan(new LinkedList<>(mDiscoveredDevices.values()));
            mHandler.postDelayed(this, BATCHED_SCAN_INTERVAL);
        }
    };
    private boolean mHasRestartRequest;

    public ScanController(@NonNull Listener listener) {
        mHandler = new Handler();
        mListener = listener;
        mOHQDeviceManager = OHQDeviceManager.sharedInstance();
    }

    public void setFilteringDeviceCategory(@Nullable final OHQDeviceCategory deviceCategory) {
        mHandler.post(() -> _setFilteringDeviceCategory(deviceCategory));
    }

    public void startScan() {
        mHandler.post(() -> _startScan(mFilteringDeviceCategory));
    }

    public void stopScan() {
        mHandler.post(() -> _stopScan());
    }

    private void _setFilteringDeviceCategory(@Nullable final OHQDeviceCategory deviceCategory) {
        if (null != deviceCategory) {
            Log.d(TAG, deviceCategory.name());
        } else {
            Log.d(TAG, "null");
        }
        mFilteringDeviceCategory = deviceCategory;
        if (mIsScanning) {
            mHasRestartRequest = true;
            _stopScan();
        }
    }

    private void _startScan(@Nullable OHQDeviceCategory filteringDeviceCategory) {
        Log.d(TAG, "_startScan");

        if (mIsScanning) {
            Log.e(TAG, "Already scanning.");
            return;
        }

        List<OHQDeviceCategory> scanFilter = new ArrayList<>();
        if (null != filteringDeviceCategory) {
            Log.d(TAG, "filteringDeviceCategory:" + filteringDeviceCategory);
            scanFilter.add(filteringDeviceCategory);
        }

        mOHQDeviceManager.scanForDevicesWithCategories(
                scanFilter,
                deviceInfo -> mHandler.post(() -> _onScan(deviceInfo)),
                reason -> mHandler.post(() -> _onScanCompletion(reason)));

        mIsScanning = true;
        mDiscoveredDevices.clear();
        mHandler.postDelayed(mBatchedScanRunnable, BATCHED_SCAN_INTERVAL);
    }

    private void _stopScan() {
        Log.d(TAG, "_stopScan");

        if (!mIsScanning) {
            return;
        }

        mOHQDeviceManager.stopScan();
    }

    private void _onScan(@NonNull final Map<OHQDeviceInfoKey, Object> deviceInfo) {
        if (!mIsScanning) {
            Log.e(TAG, "Scanning is stopped.");
            return;
        }

        final String address;
        if (!deviceInfo.containsKey(OHQDeviceInfoKey.AddressKey)) {
            throw new AndroidRuntimeException("The address must be present.");
        }
        if (null == (address = Types.autoCast(deviceInfo.get(OHQDeviceInfoKey.AddressKey)))) {
            throw new AndroidRuntimeException("The address must be present.");
        }

        final DiscoveredDevice discoveredDevice;
        if (mDiscoveredDevices.containsKey(address)) {
            Log.d(TAG, "Update discovered device. " + address);
            discoveredDevice = mDiscoveredDevices.get(address);
        } else {
            Log.d(TAG, "New discovered device. " + address);
            discoveredDevice = new DiscoveredDevice(address);
        }

        if (deviceInfo.containsKey(OHQDeviceInfoKey.AdvertisementDataKey)) {
            List<ADStructure> advertisementData = Types.autoCast(deviceInfo.get(OHQDeviceInfoKey.AdvertisementDataKey));
            discoveredDevice.setAdvertisementData(advertisementData);
        }
        if (deviceInfo.containsKey(OHQDeviceInfoKey.CategoryKey)) {
            OHQDeviceCategory deviceCategory = Types.autoCast(deviceInfo.get(OHQDeviceInfoKey.CategoryKey));
            discoveredDevice.setDeviceCategory(deviceCategory);
        }
        if (deviceInfo.containsKey(OHQDeviceInfoKey.RSSIKey)) {
            int rssi = Types.autoCast(deviceInfo.get(OHQDeviceInfoKey.RSSIKey));
            discoveredDevice.setRssi(rssi);
        }
        if (deviceInfo.containsKey(OHQDeviceInfoKey.ModelNameKey)) {
            String modelName = Types.autoCast(deviceInfo.get(OHQDeviceInfoKey.ModelNameKey));
            discoveredDevice.setModelName(modelName);
        }
        if (deviceInfo.containsKey(OHQDeviceInfoKey.LocalNameKey)) {
            String localName = Types.autoCast(deviceInfo.get(OHQDeviceInfoKey.LocalNameKey));
            discoveredDevice.setLocalName(localName);
        }

        mDiscoveredDevices.put(address, discoveredDevice);
    }

    private void _onScanCompletion(@NonNull final OHQCompletionReason reason) {
        Log.d(TAG, reason.name());

        mHandler.removeCallbacks(mBatchedScanRunnable);
        mIsScanning = false;

        if (mHasRestartRequest) {
            mHasRestartRequest = false;
            _startScan(mFilteringDeviceCategory);
        } else {
            mListener.onScanCompletion(reason);
        }
    }

    private void _onBatchedScan(@NonNull List<DiscoveredDevice> discoveredDevices) {
        if (!mIsScanning) {
            Log.e(TAG, "Scanning is stopped.");
            return;
        }
        Log.d(TAG, "discoveredDevices: " + discoveredDevices.toString());
        mListener.onScan(discoveredDevices);
    }

    public interface Listener {
        void onScan(@NonNull List<DiscoveredDevice> discoveredDevices);
        void onScanCompletion(@NonNull final OHQCompletionReason reason);
    }
}