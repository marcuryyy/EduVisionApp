package com.example.testproject

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.Objdetect
import org.opencv.objdetect.Objdetect.DICT_5X5_100


class CheckQuestionActivity : CameraActivity() {
    init {
        System.loadLibrary("opencv_java4")
    }
    private lateinit var cameraBridgeViewBase: CameraBridgeViewBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_question)

        if (!getPermissionCamera(this)) {
            return // Если разрешение не получено, выйти из метода
        }

        cameraBridgeViewBase = findViewById(R.id.camera_view)

        cameraBridgeViewBase.setCvCameraViewListener(MyCameraListener())
        cameraBridgeViewBase.enableView()

    }

    override fun getCameraViewList(): List<CameraBridgeViewBase> {
        return listOf(cameraBridgeViewBase)
    }


    private fun getPermissionCamera(activity: Activity?): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                101
            )
            return false
        } else {
            return true
        }
    }

    class MyCameraListener : CameraBridgeViewBase.CvCameraViewListener2 {
        private lateinit var mRgba: Mat
        private val mGray: Mat = Mat()
        private var text_to_put: String = ""
        override fun onCameraViewStarted(width: Int, height: Int) {

        }

        override fun onCameraViewStopped() {

        }


        override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {

            val rgbaMat = inputFrame?.rgba() ?: return Mat()
            val angle = 270.0
            val rotationMatrix = Imgproc.getRotationMatrix2D(
                Point(rgbaMat.cols() / 2.0, rgbaMat.rows() / 2.0),
                angle,
                2.0
            )

            val rotatedMat = Mat()
            Imgproc.warpAffine(rgbaMat, rotatedMat, rotationMatrix, rgbaMat.size())

            val Aruco = ArucoDetector(Objdetect.getPredefinedDictionary(DICT_5X5_100))
            // Convert to grayscale for Aruco detection
            val grayMat = Mat()
            Imgproc.cvtColor(rotatedMat, grayMat, Imgproc.COLOR_RGBA2GRAY)
            Imgproc.cvtColor(rotatedMat, rotatedMat, Imgproc.COLOR_RGBA2RGB)
            // Detect markers
            val markerCorners = ArrayList<Mat>()
            val markerIds = Mat()

            Aruco.detectMarkers(grayMat, markerCorners, markerIds)
            // Draw bounding boxes around detected markers
            if (markerIds.rows() > 0) {
                for (i in 0 until markerCorners.size) {
                    val corners = markerCorners[i]
                    val id = markerIds.get(i, 0)[0].toString()
                    val sliced_id = id.slice(0..id.length-3)
                       // val corners2 = corners.row(1)
                    if (corners.rows() >= 1) {

                       // val topLeftCorner = corners.row(0)
                        val topLeftX = corners.get(0, 0)[0]
                        val topLeftY = corners.get(0, 0)[1]
                        val bottomRightX = corners.get(0, 2)[0]
                        val bottomRightY = corners.get(0, 2)[1]

                      //  val bottomRightCorner = corners2.row(2)
                      //  Log.d("corners", bottomRightCorner.toString())


                        if (topLeftX < bottomRightX && topLeftY < bottomRightY) {
                            text_to_put = "up"
                        } else if (topLeftX > bottomRightX && topLeftY < bottomRightY) {
                            text_to_put = "right"
                        } else if (topLeftX > bottomRightX && topLeftY > bottomRightY) {
                            text_to_put = "down"
                        } else if (topLeftX < bottomRightX && topLeftY > bottomRightY) {
                            text_to_put = "left"
                        }

                        Imgproc.putText(
                            rotatedMat,
                            text_to_put,
                            Point(topLeftX, topLeftY),
                            Imgproc.FONT_HERSHEY_SIMPLEX,
                            2.0,
                            Scalar(0.0, 255.0, 0.0)
                        )
                    }
                }
                }
                //Objdetect.drawDetectedMarkers(rgbaMat, markerCorners, markerIds)
            return rotatedMat
            }


        }
    }
