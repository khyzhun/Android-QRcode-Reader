package com.sashakhyzhun.qrcodescanner

import android.Manifest
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import android.widget.TextView
import android.view.SurfaceView
import com.google.android.gms.vision.CameraSource
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import java.io.IOException
import android.view.SurfaceHolder
import android.os.Vibrator
import com.google.android.gms.vision.Detector




class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {

    companion object {
        const val requestCameraPermissionID = 1001
    }

    private lateinit var cameraViewer: SurfaceView
    private lateinit var textResult: TextView
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraViewer = findViewById(R.id.cameraViewer)
        textResult = findViewById(R.id.textResult)

        barcodeDetector = BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .build()


        cameraViewer.holder.addCallback(this)
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}
            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val qrCodes = detections.detectedItems
                if (qrCodes.size() != 0) {
                    textResult.apply {
                        //Create vibrate
                        val vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibrator.vibrate(1000)
                        textResult.text = qrCodes.valueAt(0).displayValue
                    }
                }
            }
        })
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            requestCameraPermissionID -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(
                                    this,
                                    android.Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        return
                    }

                    try {
                        cameraSource.start(cameraViewer.holder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }


    /**
     * SURFACE OVERRIDE
     */
    override fun surfaceCreated(holder: SurfaceHolder) {
        if (ActivityCompat.checkSelfPermission(this@MainActivity,
                    android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            //Request Permission
            ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.CAMERA),
                    requestCameraPermissionID
            )
            return
        }

        try {
            cameraSource.start(cameraViewer.holder)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        cameraSource.stop()
    }

}
