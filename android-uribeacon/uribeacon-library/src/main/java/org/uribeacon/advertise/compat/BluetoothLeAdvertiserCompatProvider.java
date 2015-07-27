/*
 * Copyright 2014 Google Inc. All rights reserved.
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

import static android.content.Context.BLUETOOTH_SERVICE;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;

public class BluetoothLeAdvertiserCompatProvider {

  private static BluetoothLeAdvertiserCompat advertiserInstance;

  private BluetoothLeAdvertiserCompatProvider() {
  }

  @Nullable
  public static synchronized BluetoothLeAdvertiserCompat getBluetoothLeAdvertiserCompat(Context context) {
    return getBluetoothLeAdvertiserCompat(context, true);
  }

  @Nullable
  public static synchronized BluetoothLeAdvertiserCompat getBluetoothLeAdvertiserCompat(
          Context context, boolean canUseNativeApi) {
    if (advertiserInstance == null) {
      BluetoothManager bluetoothManager =
              (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);

      if (bluetoothManager != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && canUseNativeApi
                && areHardwareFeaturesSupported(bluetoothManager)) {
          advertiserInstance = new LBluetoothLeAdvertiserCompat(bluetoothManager);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
          advertiserInstance = new JbBluetoothLeAdvertiserCompat(bluetoothManager);
        }
      }
    }
    return advertiserInstance;
  }

  /**
   * Check that the hardware has indeed the features used by the L-specific implementation.
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static boolean areHardwareFeaturesSupported(BluetoothManager bluetoothManager) {
    BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
    return bluetoothAdapter != null
            && bluetoothAdapter.isMultipleAdvertisementSupported();
  }
}
