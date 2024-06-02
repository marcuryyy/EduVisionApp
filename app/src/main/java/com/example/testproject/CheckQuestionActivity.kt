package com.example.testproject

import android.Manifest
import android.content.Context
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
    private lateinit var student_names: ArrayList<String>
    private lateinit var aruco_ids: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_question)
        if (hasCameraPermission()) {
            cameraBridgeViewBase = findViewById(R.id.camera_view)
            val results_button: Button = findViewById(R.id.results_button)
            cameraBridgeViewBase.setCvCameraViewListener(MyCameraListener(this))
            cameraBridgeViewBase.enableView()

            aruco_ids = intent.getStringArrayListExtra("aruco_id") ?: return
            student_names = intent.getStringArrayListExtra("student_name") ?: return
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

    private inner class MyCameraListener(private val context: Context) : CameraBridgeViewBase.CvCameraViewListener2 {
        private var text_to_put: String = ""
        val test_id = intent.getStringExtra("test_id")
        val db_tests = DBtests(context, null)
        val right_answers: List<String> = db_tests.getTestRightAnswer(test_id.toString())

        override fun onCameraViewStarted(width: Int, height: Int) {

        }

        override fun onCameraViewStopped() {

        }


        override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {

            val rgbaMat = inputFrame?.rgba() ?: return Mat()

            val Aruco = ArucoDetector(Objdetect.getPredefinedDictionary(DICT_5X5_100))
            val student_list_dict: Map<String, String> = aruco_ids.zip(student_names).toMap()

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
                        id_map[sliced_id] = text_to_put
                        val answer_results = id_map.keys.zip(id_map.values).toMap()
                        for (aruco_id in answer_results.keys){
                            if(student_list_dict.size > 0) {
                                val student_name = student_list_dict[aruco_id]
                                if (student_name != null) {
                                    if (answer_results[aruco_id] in right_answers) {
                                        Imgproc.circle(
                                            rgbaMat,
                                            Point(topLeftX, topLeftY),
                                            10,
                                            Scalar(0.0,255.0,0.0),
                                            -1
                                        )
                                    }
                                    else {
                                        Imgproc.circle(
                                            rgbaMat,
                                            Point(topLeftX, topLeftY),
                                            10,
                                            Scalar(255.0,0.0,0.0),
                                            -1
                                        )
                                    }
                                }
                            }

                        }
                    }
                }
                }
            return rgbaMat
            }


        }
    }
