package com.bq.camerabq.view

import android.view.OrientationEventListener
import com.bq.camerabq.MainActivity
import com.bq.camerabq.Store
import com.bq.camerabq.redux.Reducer
import com.bq.camerabq.state.Action
import com.bq.camerabq.state.AppState

data class RotationState(
        val absoluteRotation: Float = 0f
)

data class DeviceRotatedAction(val degrees: Int) : Action

class RotationStateReducer : Reducer<AppState, Action> {
    override fun reduce(state: AppState, action: Action): AppState {
        val rotationSTate = with(state.rotationState) {
            when (action) {
                is DeviceRotatedAction -> {
                    copy(absoluteRotation = action.degrees.toFloat())
                }

                else -> this
            }
        }
        return state.copy(rotationState = rotationSTate)
    }
}

class RotationController(val hostActivity: MainActivity) {
    val orientationListener: OrientationEventListener

    init {
        orientationListener = object : OrientationEventListener(hostActivity) {
            var lastBucketRotation = 0
            var lastBucket = 0
            var accumulatedRotation = 0

            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val rotation = orientation
                val bucket = when (rotation) {
                    in 315..360 -> 0
                    in 0..45 -> 0
                    in 45..135 -> 1  // Right
                    in 135..225 -> 2 // Upside down
                    in 225..315 -> 3  // Left
                    else -> 0
                }

                val rotationDiff = Math.abs(rotation - lastBucketRotation)

                if (bucket != lastBucket && rotationDiff > 30) {
                    val rotationSteps = if (lastBucket == 3 && bucket == 0) -1
                    else if (lastBucket == 0 && bucket == 3) 1 //Need to hardcode both corner cases
                    else lastBucket - bucket //This will be 1, -1, 2, -2, depending on direction


                    accumulatedRotation += rotationSteps * 90
                    lastBucket = bucket
                    lastBucketRotation = rotation

                    Store.dispatch(DeviceRotatedAction(accumulatedRotation))

                }
            }
        }
    }

    fun enable() {
        orientationListener.enable()
    }

    fun disable() {
        orientationListener.disable()
    }

}