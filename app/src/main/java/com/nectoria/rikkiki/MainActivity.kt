package com.nectoria.rikkiki

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity() {
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var cameraSelector: CameraSelector? = null
    private lateinit var detector: FaceDetector
    private lateinit var poseDetector: PoseDetector
    private var previewView: PreviewView? = null
    private lateinit var textView: TextView
    private lateinit var seek: SeekBar
    private lateinit var killBtn: Button
    private lateinit var spawnBtn: Button
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var gameViewAnts: GameViewAnts
    private var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE
            )
        }

        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        setContentView(R.layout.activity_camera)
        //previewView = findViewById(R.id.test)
        gameViewAnts = findViewById(R.id.gameViewAnts)
        killBtn = findViewById(R.id.btn)
        spawnBtn = findViewById(R.id.Spawn)
        textView = findViewById(R.id.textView)
        cameraExecutor = Executors.newSingleThreadExecutor()

        killBtn.setOnClickListener {
            gameViewAnts.killRandomAnt()
        }
        spawnBtn.setOnClickListener {
            gameViewAnts.spawnAnt()
        }

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
        detector = FaceDetection.getClient(options)

        val poseOptions = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        poseDetector = PoseDetection.getClient(poseOptions)

        startCamera()
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            needUpdateGraphicOverlayImageSourceInfo = true

            val analyser = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        ContextCompat.getMainExecutor(this),
                        ImageAnalysis.Analyzer { imageProxy: ImageProxy ->
                            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val inputImage =
                                    InputImage.fromMediaImage(mediaImage, rotationDegrees)
                                mediaImage?.let {
                                    /*detector.process(inputImage)
                                        .addOnSuccessListener { results ->
                                            textView.text = results.size.toString()
                                            imageProxy.close()
                                        }
                                        .addOnFailureListener {
                                            imageProxy.close()
                                        }
                                     */
                                    poseDetector.process(inputImage)
                                        .addOnSuccessListener { results ->
                                            textView.text = results.allPoseLandmarks.size.toString()
                                            imageProxy.close()
                                        }
                                        .addOnFailureListener {
                                            imageProxy.close()
                                        }
                                }
                            }
                        }
                    )
                }

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView?.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, analyser
                )

            } catch (exc: Exception) {
                Log.e("TEST", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}