package com.example.testproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.processNextEventInCurrentThread
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.Objdetect
import org.opencv.objdetect.Objdetect.DICT_5X5_100
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.graphics.Color
import kotlin.properties.Delegates

class CheckQuestionActivity : AppCompatActivity() {

    init {
        System.loadLibrary("opencv_java4")
    }

    private lateinit var viewFinder: PreviewView

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var next_button: Button
    private lateinit var prev_button: Button
    private var id_map: MutableMap<String, String> = mutableMapOf()
    val bundle = Bundle()
    private lateinit var student_names: ArrayList<String>
    private lateinit var aruco_ids: ArrayList<String>
    private lateinit var right_answers: List<String>
    protected var questionNum: Int = 0
    private lateinit var questionList: ArrayList<String>
    private lateinit var db_tests: DBtests
    private var questionResults: ArrayList<MutableMap<String, String>> = ArrayList()
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "CameraXApp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_question)

        val results_button: Button = findViewById(R.id.results_button)
        db_tests = DBtests(this, null)
        viewFinder = findViewById(R.id.viewFinder)
        next_button = findViewById(R.id.next_button)
        prev_button = findViewById(R.id.prev_button)
        questionList = intent.getStringArrayListExtra("questionsArray") ?: arrayListOf()
        requestCameraPermission()

        cameraExecutor = Executors.newSingleThreadExecutor()

        aruco_ids = intent.getStringArrayListExtra("aruco_id") ?: return
        student_names = intent.getStringArrayListExtra("student_name") ?: return
        val test_id = intent.getStringExtra("test_id")
        if(intent.getBooleanExtra("allTests", false) == true){
            val id = db_tests.getTestId(questionList[questionNum])
            right_answers = db_tests.getTestRightAnswer(id.getString("test_id").toString())
            next_button.visibility = View.VISIBLE
            prev_button.visibility = View.VISIBLE
        }
        else{
            results_button.visibility = View.VISIBLE
            questionList += db_tests.getTestText(test_id.toString())
            right_answers = db_tests.getTestRightAnswer(test_id.toString())
        }


        results_button.setOnClickListener{

            if(intent.getBooleanExtra("allTests", false) == true){
                val idMapCopy = id_map.toMutableMap()
                if (questionNum < questionResults.size){
                    questionResults[questionNum] = idMapCopy
                }
                else{
                    questionResults += idMapCopy
                }
                val intent = Intent(this, questionAfterCheckActivity::class.java)
                bundle.putStringArrayList("aruco_id", aruco_ids)
                bundle.putStringArrayList("student_name", student_names)
                intent.putExtra("questionResults", questionResults)
                intent.putStringArrayListExtra("questions", questionList)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            else {
                val intent = Intent(this, ResultsActivity::class.java)
                bundle.putStringArrayList("aruco_id", aruco_ids)
                bundle.putStringArrayList("student_name", student_names)
                bundle.putStringArrayList("keys", ArrayList(id_map.keys))
                bundle.putStringArrayList("values", ArrayList(id_map.values.map { it.toString() }))
                bundle.putString("test_id", test_id)
                intent.putExtras(bundle)

                startActivity(intent)
            }
        }

        next_button.setOnClickListener{
            val idMapCopy = id_map.toMutableMap()
            if (questionNum < questionResults.size){
                questionResults[questionNum] = idMapCopy
            }
            else{
                questionResults += idMapCopy
            }
            if (questionNum + 1 < questionList.size) {
                questionNum++
                val id = db_tests.getTestId(questionList[questionNum])
                right_answers = db_tests.getTestRightAnswer(id.getString("test_id").toString())
            }
            if (questionNum + 1 == questionList.size){
                results_button.visibility = View.VISIBLE
            }
        }
        prev_button.setOnClickListener{
            val idMapCopy = id_map.toMutableMap()
            if (questionNum < questionResults.size){
                questionResults[questionNum] = idMapCopy
            }
            else{
                questionResults += idMapCopy
            }
            results_button.visibility = View.GONE
            if (questionNum - 1 >= 0) {
                questionNum--
                val id = db_tests.getTestId(questionList[questionNum])
                right_answers = db_tests.getTestRightAnswer(id.getString("test_id").toString())
            }
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)



        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Создаем ImageAnalysis use case
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, FpsAnalyzer {
                        runOnUiThread {
                        }
                    })
                }

            // Выбираем заднюю камеру по умолчанию
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Отвязываем все use cases перед привязкой новых
                cameraProvider.unbindAll()

                // Привязываем use cases к жизненному циклу
                cameraProvider.bindToLifecycle(
                    this, // LifecycleOwner
                    cameraSelector, // CameraSelector
                    preview, // UseCase
                    imageAnalyzer // UseCase
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }



    private inner class FpsAnalyzer(private val listener: (Double) -> Unit) : ImageAnalysis.Analyzer {
        private var lastFrameTime = 0L
        private var frameCount = 0
        private var text_to_put: String = ""

        var questionText: TextView = findViewById(R.id.questionText)

        fun ImageProxy.yuvToRgba(): Mat {
            val rgbaMat = Mat()
            if (format == ImageFormat.YUV_420_888
                && planes.size == 3) {

                val chromaPixelStride = planes[1].pixelStride

                if (chromaPixelStride == 2) {
                    assert(planes[0].pixelStride == 1)
                    assert(planes[2].pixelStride == 2)
                    val yPlane = planes[0].buffer
                    val uvPlane1 = planes[1].buffer
                    val uvPlane2 = planes[2].buffer
                    val yMat = Mat(height, width, CvType.CV_8UC1, yPlane)
                    val uvMat1 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane1)
                    val uvMat2 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane2)
                    val addrDiff = uvMat2.dataAddr() - uvMat1.dataAddr()
                    if (addrDiff > 0) {
                        assert(addrDiff == 1L)
                        Imgproc.cvtColorTwoPlane(yMat, uvMat1, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV12)
                    } else {
                        assert(addrDiff == -1L)
                        Imgproc.cvtColorTwoPlane(yMat, uvMat2, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21)
                    }
                } else {
                    val yuvBytes = ByteArray(width * (height + height / 2))
                    val yPlane = planes[0].buffer
                    val uPlane = planes[1].buffer
                    val vPlane = planes[2].buffer

                    yPlane.get(yuvBytes, 0, width * height)

                    val chromaRowStride = planes[1].rowStride
                    val chromaRowPadding = chromaRowStride - width / 2

                    var offset = width * height
                    if (chromaRowPadding == 0) {
                        uPlane.get(yuvBytes, offset, width * height / 4)
                        offset = offset.plus(width * height / 4)
                        vPlane.get(yuvBytes, offset, width * height / 4)
                    } else {
                        for (i in 0 until height / 2) {
                            uPlane.get(yuvBytes, offset, width / 2)
                            offset = offset.plus(width / 2)
                            if (i < height / 2 - 1) {
                                uPlane.position(uPlane.position() + chromaRowPadding)
                            }
                        }
                        for (i in 0 until height / 2) {
                            vPlane.get(yuvBytes, offset, width / 2)
                            offset = offset.plus(width / 2)
                            if (i < height / 2 - 1) {
                                vPlane.position(vPlane.position() + chromaRowPadding)
                            }
                        }
                    }

                    val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
                    yuvMat.put(0, 0, yuvBytes)
                    Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_I420, 4)
                }
            }

            return rgbaMat
        }

        override fun analyze(imageProxy: ImageProxy) {
            val overlayView: OverlayView = findViewById(R.id.overlayView)
            overlayView.invalidate()
            runOnUiThread {
                questionText.text = questionList[questionNum]
            }
            val currentTime = System.currentTimeMillis()

            if (lastFrameTime != 0L) {
                frameCount++
                val timeElapsed = currentTime - lastFrameTime
                if (timeElapsed >= 1000) {
                    val fps = frameCount * 1000.0 / timeElapsed
                    listener(fps)
                    frameCount = 0
                    lastFrameTime = currentTime
                }
            } else {
                lastFrameTime = currentTime
            }

            val rgbaMat = imageProxy.yuvToRgba()


            val Aruco = ArucoDetector(Objdetect.getPredefinedDictionary(DICT_5X5_100))

            val grayMat = Mat()
            Imgproc.cvtColor(rgbaMat, grayMat, Imgproc.COLOR_RGBA2GRAY)
            Imgproc.cvtColor(rgbaMat, rgbaMat, Imgproc.COLOR_RGBA2RGB)

            val markerCorners = ArrayList<Mat>()
            val markerIds = Mat()
            val positionsToDrawOn: MutableList<Point> = mutableListOf()
            Aruco.detectMarkers(grayMat, markerCorners, markerIds)
            val scaleX = overlayView.width / rgbaMat.size().width // scale по Х работает нормально
            val scaleY = overlayView.height / rgbaMat.size().height // делаю скейл по У и при умножении далее почему-то все плохо
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
                        positionsToDrawOn += Point(topLeftX, topLeftY)
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
                        if (overlayView.circles.find { it.id == sliced_id } == null) {
                            if (text_to_put in right_answers){
                                overlayView.addCircle(sliced_id, ((topLeftX + bottomRightX)/2) * scaleX,
                                    ((topLeftY + bottomRightY)/2) * scaleY, 5f, Color.GREEN)
                            }
                            else{
                                overlayView.addCircle(sliced_id, ((topLeftX + bottomRightX)/2) * scaleX,
                                    ((topLeftY + bottomRightY)/2) * scaleY, 5f, Color.RED)
                            }

                        } else {

                            if (text_to_put in right_answers){
                                overlayView.updateCircleColor(sliced_id, Color.GREEN)
                                overlayView.updateCircle(sliced_id,  ((topLeftX + bottomRightX)/2) * scaleX,
                                    ((topLeftY + bottomRightY)/2) * scaleY, 5f)
                            }
                            else{
                                overlayView.updateCircleColor(sliced_id, Color.RED)
                                overlayView.updateCircle(sliced_id,  ((topLeftX + bottomRightX)/2) * scaleX,
                                    ((topLeftY + bottomRightY)/2) * scaleY, 5f)
                            }


                        }


                    }


                }


            }


            else {
                overlayView.removeCircles()
            }


            imageProxy.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
