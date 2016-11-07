package com.bq.camerabq


import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager

import com.bq.camerabq.hardware.CameraStateReducer
import com.bq.camerabq.hardware.CamerasIdentifiedAction
import com.bq.camerabq.state.AppState
import com.bq.camerabq.view.RotationStateReducer
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Store.init(AppState())
        Store.addReducer(CameraStateReducer())
        Store.addReducer(RotationStateReducer())

        //Map available camera information
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Store.dispatch(CamerasIdentifiedAction(availableCameraMap(cameraManager)))
    }

    companion object {
        private lateinit var INSTANCE: App
        fun get(): App {
            return INSTANCE
        }
    }
}
