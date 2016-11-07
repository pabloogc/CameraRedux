package com.bq.camerabq.hardware

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import com.bq.camerabq.Store

data class PhotoCaptureHolder(
        val captureWidth: Int,
        val captureHeight: Int) {

    fun createPreviewCaptureRequest(): CaptureRequest.Builder = Store
            .state
            .cameraState
            .cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            .apply {
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            }

    val imageReader: ImageReader by lazy {
        ImageReader.newInstance(captureWidth, captureHeight, ImageFormat.JPEG, 2)
    }

}