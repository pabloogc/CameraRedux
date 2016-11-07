package com.bq.camerabq

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager


fun availableCameraMap(cameraManager: CameraManager): Map<String, CameraCharacteristics> {
    return cameraManager.cameraIdList.map {
        it to cameraManager.getCameraCharacteristics(it)
    }.toMap()
}