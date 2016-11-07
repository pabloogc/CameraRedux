package com.bq.camerabq.state

import com.bq.camerabq.hardware.CameraState
import com.bq.camerabq.view.RotationState

data class AppState(
        val cameraState: CameraState = CameraState(),
        val rotationState: RotationState = RotationState()) {
}
