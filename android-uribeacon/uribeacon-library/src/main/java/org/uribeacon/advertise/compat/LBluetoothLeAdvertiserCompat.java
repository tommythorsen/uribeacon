/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uribeacon.advertise.compat;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.SparseArray;

import java.util.Map;
import java.util.HashMap;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class LBluetoothLeAdvertiserCompat extends BluetoothLeAdvertiserCompat {

    private final Map<AdvertiseCallback, android.bluetooth.le.AdvertiseCallback> callbacksMap =
        new HashMap<AdvertiseCallback, android.bluetooth.le.AdvertiseCallback>();

    private final android.bluetooth.le.BluetoothLeAdvertiser osAdvertiser;

    LBluetoothLeAdvertiserCompat(BluetoothManager manager) {
        osAdvertiser = manager.getAdapter().getBluetoothLeAdvertiser();
    }

    @Override
    public void startAdvertising(AdvertiseSettings settings,
            AdvertiseData advertiseData, AdvertiseCallback callback)
    {
        if (callbacksMap.containsKey(callback)) {
            stopAdvertising(callback);
        }

        android.bluetooth.le.AdvertiseCallback osCallback = toOs(callback);
        callbacksMap.put(callback, osCallback);

        osAdvertiser.startAdvertising(
                toOs(settings), toOs(advertiseData), osCallback);
    }

    @Override
    public void startAdvertising(AdvertiseSettings settings,
            AdvertiseData advertiseData, AdvertiseData scanResponse, AdvertiseCallback callback)
    {
        if (callbacksMap.containsKey(callback)) {
            stopAdvertising(callback);
        }

        android.bluetooth.le.AdvertiseCallback osCallback = toOs(callback);
        callbacksMap.put(callback, osCallback);

        osAdvertiser.startAdvertising(
                toOs(settings), toOs(advertiseData), toOs(scanResponse), osCallback);
    }

    @Override
    public void stopAdvertising(AdvertiseCallback callback) {
        android.bluetooth.le.AdvertiseCallback osCallback = callbacksMap.get(callback);

        if (osCallback != null) {
            osAdvertiser.stopAdvertising(osCallback);
            callbacksMap.remove(callback);
        }
    }

    private static android.bluetooth.le.AdvertiseCallback toOs(final AdvertiseCallback callback) {
        return new android.bluetooth.le.AdvertiseCallback() {
            @Override
            public void onStartSuccess(android.bluetooth.le.AdvertiseSettings settingsInEffect) {
                callback.onStartSuccess(fromOs(settingsInEffect));
            }

            @Override
            public void onStartFailure(int errorCode) {
                callback.onStartFailure(errorCode);
            }
        };
    }

    private static android.bluetooth.le.AdvertiseSettings toOs(AdvertiseSettings settings) {
        return new android.bluetooth.le.AdvertiseSettings.Builder()
            .setAdvertiseMode(settings.getMode())
            .setTxPowerLevel(settings.getTxPowerLevel())
            .setConnectable(settings.isConnectable())
            .setTimeout(settings.getTimeout())
            .build();
    }

    private static AdvertiseSettings fromOs(android.bluetooth.le.AdvertiseSettings osSettings) {
        return new AdvertiseSettings.Builder()
            .setAdvertiseMode(osSettings.getMode())
            .setTxPowerLevel(osSettings.getTxPowerLevel())
            .setConnectable(osSettings.isConnectable())
            .setTimeout(osSettings.getTimeout())
            .build();
    }

    private static android.bluetooth.le.AdvertiseData toOs(AdvertiseData data) {
        android.bluetooth.le.AdvertiseData.Builder builder = new android.bluetooth.le.AdvertiseData.Builder();
        for (ParcelUuid serviceUuid : data.getServiceUuids()) {
            builder.addServiceUuid(serviceUuid);
        }

        SparseArray<byte[]> manufacturerSpecificData = data.getManufacturerSpecificData();
        for (int i = 0; i < manufacturerSpecificData.size(); ++i) {
            builder.addManufacturerData(manufacturerSpecificData.keyAt(i),
                    manufacturerSpecificData.valueAt(i));
        }

        Map<ParcelUuid, byte[]> serviceData = data.getServiceData();
        for (ParcelUuid uuid : serviceData.keySet()) {
            builder.addServiceData(uuid, serviceData.get(uuid));
        }

        builder.setIncludeTxPowerLevel(data.getIncludeTxPowerLevel());
        builder.setIncludeDeviceName(data.getIncludeDeviceName());

        return builder.build();
    }
}
