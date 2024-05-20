package com.example.testproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.Objdetect
import org.opencv.objdetect.Objdetect.DICT_5X5_100


class CheckQuestionActivity : CameraActivity() {
    init {
        System.loadLibrary("opencv_java4")
    }
    private lateinit var cameraBridgeViewBase: CameraBridgeViewBase
    private var id_map: MutableMap<String, String> = mutableMapOf()
    val bundle = Bundle()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_question)
        if (hasCameraPermission()) {
            cameraBridgeViewBase = findViewById(R.id.camera_view)
            val results_button: Button = findViewById(R.id.results_button)
            cameraBridgeViewBase.setCvCameraViewListener(MyCameraListener())
            cameraBridgeViewBase.enableView()

            val aruco_ids = intent.getStringArrayListExtra("aruco_id") ?: return
            val student_names = intent.getStringArrayListExtra("student_name") ?: return
            val test_id = intent.getStringExtra("test_id")
            results_button.setOnClickListener{
                bundle.putStringArrayList("aruco_id", aruco_ids)
                bundle.putStringArrayList("student_name", student_names)
                bundle.putStringArrayList("keys", ArrayList(id_map.keys))
                bundle.putStringArrayList("values", ArrayList(id_map.values.map { it.toString() }))
                bundle.putString("test_id", test_id)
                val intent = Intent(this, ResultsActivity::class.java)

                intent.putExtras(bundle)

                startActivity(intent)
            }
        }
    }



    override fun getCameraViewList(): List<CameraBridgeViewBase> {
        return listOf(cameraBridgeViewBase)
    }


    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private inner class MyCameraListener : CameraBridgeViewBase.CvCameraViewListener2 {
        private var text_to_put: String = ""

        override fun onCameraViewStarted(width: Int, height: Int) {
            println("zxc")
            val desiredSize = Size(1920.0, 1080.0)
            cameraBridgeViewBase.setMaxFrameSize(desiredSize.width.toInt(), desiredSize.height.toInt())
        }

        override fun onCameraViewStopped() {

        }


        override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {

            val rgbaMat = inputFrame?.rgba() ?: return Mat()


            val Aruco = ArucoDetector(Objdetect.getPredefinedDictionary(DICT_5X5_100))

            val grayMat = Mat()
            Imgproc.cvtColor(rgbaMat, grayMat, Imgproc.COLOR_RGBA2GRAY)
            Imgproc.cvtColor(rgbaMat, rgbaMat, Imgproc.COLOR_RGBA2RGB)

            val markerCorners = ArrayList<Mat>()
            val markerIds = Mat()

            Aruco.detectMarkers(grayMat, markerCorners, markerIds)

            if (markerIds.rows() > 0) {
                for (i in 0 until markerCorners.size) {
                    val corners = markerCorners[i]
                    val id = markerIds.get(i, 0)[0].toString()
                    val sliced_id = id.slice(0..id.length-3)
                    if (corners.rows() >= 1) {

                        val topLeftX = corners.get(0, 0)[0]
                        val topLeftY = corners.get(0, 0)[1]
                        val bottomRightX = corners.get(0, 2)[0]
                        val bottomRightY = corners.get(0, 2)[1]

                        if (topLeftX < bottomRightX && topLeftY < bottomRightY) {
                            text_to_put = "up"
                        } else if (topLeftX > bottomRightX && topLeftY < bottomRightY) {
                            text_to_put = "right"
                        } else if (topLeftX > bottomRightX && topLeftY > bottomRightY) {
                            text_to_put = "down"
                        } else if (topLeftX < bottomRightX && topLeftY > bottomRightY) {
                            text_to_put = "left"
                        }
                        id_map[sliced_id.toString()] = text_to_put
                        Imgproc.putText(
                            rgbaMat,
                            text_to_put,
                            Point(topLeftX, topLeftY),
                            Imgproc.FONT_HERSHEY_SIMPLEX,
                            2.0,
                            Scalar(0.0, 255.0, 0.0)
                        )
                    }
                }
                }
            return rgbaMat
            }


        }
    }
