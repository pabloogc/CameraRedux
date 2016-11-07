package com.bq.camerabq.hardware

import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import com.bq.camerabq.Store
import com.bq.camerabq.redux.Reducer
import com.bq.camerabq.state.Action
import com.bq.camerabq.state.AppState
import io.reactivex.Single

private const val NO_CAMERA = "NO_CAMERA"

data class CameraState(
        val photoCaptureHolder: PhotoCaptureHolder? = null,
        val lastPhotoPath: String? = null,

        val captureWidth: Int = 0,
        val captureHeight: Int = 0,
        val rotation: Int = 0,

        val surfaceReady: Boolean = false,
        val cameraOpened: Boolean = false,
        val cameraDevice: CameraDevice? = null,

        val selectedCameraId: String = NO_CAMERA,
        val availableCameras: Map<String, CameraCharacteristics> = emptyMap()) {
}


data class SurfaceChangedAction(val surface: SurfaceTexture, val width: Int, val height: Int) : Action
data class CamerasIdentifiedAction(val cameras: Map<String, CameraCharacteristics>) : Action
data class SurfaceDestroyedAction(val surface: SurfaceTexture) : Action
data class CameraOpenedAction(val cameraDevice: CameraDevice) : Action
data class CaptureSessionStarted(val cameraCaptureSession: CameraCaptureSession) : Action

class CameraClosedAction() : Action
class TakePhotoAction() : Action

class CameraStateReducer : Reducer<AppState, Action> {
    override fun reduce(state: AppState, action: Action): AppState {
        val cameraState = with(state.cameraState) {
            when (action) {

                is CamerasIdentifiedAction -> {
                    copy(
                            selectedCameraId = if (this.selectedCameraId == NO_CAMERA)
                                selectDefaultCamera(action.cameras) else NO_CAMERA,
                            availableCameras = action.cameras
                    )
                }

                is CaptureSessionStarted -> {
                    copy(
                            photoCaptureHolder = photoCaptureHolder!!.copy()
                    )
                }

                is CameraOpenedAction -> {
                    cleanUp(this)
                    copy(
                            photoCaptureHolder =
                            if (surfaceReady)
                                PhotoCaptureHolder(captureWidth, captureHeight)
                            else null,
                            cameraOpened = true,
                            cameraDevice = action.cameraDevice
                    )
                }

                is CameraClosedAction -> {
                    cleanUp(this)
                    copy(
                            photoCaptureHolder = null,
                            cameraOpened = false,
                            cameraDevice = null
                    )
                }

                is SurfaceChangedAction -> {
                    copy(
                            photoCaptureHolder =
                            if (cameraDevice != null)
                                PhotoCaptureHolder(action.width, action.height)
                            else null,
                            surfaceReady = true,
                            captureWidth = action.width,
                            captureHeight = action.height
                    )
                }

                is SurfaceDestroyedAction -> {
                    cleanUp(this)
                    copy(
                            photoCaptureHolder = null,
                            surfaceReady = false,
                            captureWidth = 0,
                            captureHeight = 0
                    )
                }

                is TakePhotoAction -> {
                    copy()
                }

                else -> this
            }
        }
        return state.copy(cameraState = cameraState)
    }

    private fun cleanUp(cameraState: CameraState) {
        cameraState.photoCaptureHolder?.imageReader?.close()
        cameraState.cameraDevice?.close()
    }

    private fun selectDefaultCamera(cameras: Map<String, CameraCharacteristics>): String {
        return cameras.entries.find {
            val (id, characteristics) = it
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                return@find false
            }
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: return@find false
            return@find true
        }?.key ?: cameras.keys.elementAt(0)
    }
}