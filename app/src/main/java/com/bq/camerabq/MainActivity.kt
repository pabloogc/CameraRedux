package com.bq.camerabq

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.Toast
import com.bq.camerabq.app.BaseActivity
import com.bq.camerabq.hardware.*
import com.bq.camerabq.view.RotationController
import kotterknife.bindView
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.*

class MainActivity : BaseActivity() {

    private val textureView: TextureView by bindView(R.id.texture_view)
    private val cameraControlsContainer: ViewGroup by bindView(R.id.camera_controls_container)
    private val takePhotoButton: View by bindView(R.id.take_photo_button)

    private val rotateAnimInterpolator = LinearInterpolator()

    private lateinit var textureSurface: Surface
    private lateinit var rotationController: RotationController
    private lateinit var cameraManager: CameraManager

    private var backgroundHandler: Handler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rotationController = RotationController(this)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        with(HandlerThread("bg")) {
            start()
            backgroundHandler = Handler(looper)
        }

        takePhotoButton.setOnClickListener {
            Store.dispatch(TakePhotoAction())
        }
    }

    override fun onResume() {
        super.onResume()

        rotationController.now { enable() } onPause { disable() }

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                val texture = textureView.surfaceTexture
                texture.setDefaultBufferSize(width, height)
                textureSurface = Surface(texture)
                Store.dispatch(SurfaceChangedAction(surface, width, height))
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                Store.dispatch(SurfaceChangedAction(surface, width, height))
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                Store.dispatch(SurfaceDestroyedAction(surface))
                return true //Returning true releases the surface automatically (we don't need to keep a still image)
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        try {
            cameraManager.openCamera(Store.state.cameraState.selectedCameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(cameraDevice: CameraDevice) {
                    Store.dispatch(CameraOpenedAction(cameraDevice))
                }

                override fun onDisconnected(cameraDevice: CameraDevice) {
                    Store.dispatch(CameraClosedAction())
                }

                override fun onError(cameraDevice: CameraDevice, i: Int) {
                    Toast.makeText(this@MainActivity, "Unable to open camera", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        Store.flowable { it.cameraState }
                .takeFirst { cameraState -> cameraState.cameraOpened && cameraState.surfaceReady }
                .subscribe({ cameraState ->
                    //Wait for both surface ready and camera opened to start the preview
                    try {
                        //Preconditions, device and capture request must be ready
                        val cameraDevice = cameraState.cameraDevice ?: return@subscribe
                        val photoCaptureHolder = cameraState.photoCaptureHolder ?: return@subscribe
                        val photoCaptureRequest = photoCaptureHolder.createPreviewCaptureRequest()
                        photoCaptureRequest.addTarget(textureSurface)

                        cameraDevice.createCaptureSession(Arrays.asList<Surface>(textureSurface, photoCaptureHolder.imageReader.surface),
                                object : CameraCaptureSession.StateCallback() {
                                    override fun onConfigured(session: CameraCaptureSession) {
                                        Store.dispatch(CaptureSessionStarted(session))
                                        try {
                                            session.setRepeatingRequest(photoCaptureRequest.build(), null, backgroundHandler)
                                        } catch (e: CameraAccessException) {
                                            e.printStackTrace()
                                        }
                                    }

                                    override fun onConfigureFailed(session: CameraCaptureSession) {
                                    }

                                }, null)

                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                })
                .track()


        Store.flowable { it.rotationState }
                .subscribe { rotationState ->
                    val targetList = (0..cameraControlsContainer.childCount - 1)
                            .map { cameraControlsContainer.getChildAt(it) }
                            .plus(takePhotoButton)

                    targetList.forEach {
                        it.animate()
                                .setDuration(333)
                                .setInterpolator(rotateAnimInterpolator)
                                .rotation(rotationState.absoluteRotation)
                    }

                }.track()
    }
}
