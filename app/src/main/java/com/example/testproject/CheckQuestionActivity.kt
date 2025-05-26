package com.example.testproject

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
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
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.properties.Delegates

@Serializable
data class QuestionResultsRequest(
    val taken_survey_id: Int,
    val taken_question_id: Int,
    val answers: List<Answer>

)

@Serializable
data class Answer(
    val student_id: Int,
    val answer: Int
)

@Serializable
data class ClassInfo(
    val id: Int,
    val name: String,
    val aruco_num: Int
)

@Serializable
data class ApiResponse(
    val status: String,
    val data: List<ClassInfo>
)
@Serializable
data class QuestionResponse(
    val status: String,
    val data: QuestionData
)

@Serializable
data class QuestionData(
    val status: String, // "next_question" или "survey_completed"
    var taken_question_id: Int? = null,
    var question_id: Int? = null,
    val question_text: String? = null,
    val options: Map<String, String>? = null,
    var taken_survey_id: Int? = null
)
open class CheckQuestionActivity : AppCompatActivity() {

    init {
        System.loadLibrary("opencv_java4")
    }




    private lateinit var viewFinder: PreviewView
    private lateinit var overlayView: OverlayView
    private var cameraExecutor = Executors.newSingleThreadExecutor()
    private var id_map: MutableMap<String, String> = mutableMapOf()
    val bundle = Bundle()
    private lateinit var student_names: ArrayList<String>
    private lateinit var aruco_ids: ArrayList<Int>
    private var right_answers = mutableListOf<String>()
    private var question_ids = mutableListOf<Int>()
    private var questionNum: Int = 0
    private var questionList = mutableListOf<String>()
    private lateinit var db_tests: DBtests
    private var questionResults: ArrayList<MutableMap<String, String>> = ArrayList()
    private var studentsAnswers: List<Answer> = emptyList()
    private lateinit var questionText: TextView
    private lateinit var arucoToStudentMap: Map<Int, Int>
    private var currentQuestionData: QuestionData? = QuestionData("")
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "CameraXApp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_question)
        requestCameraPermission()

        db_tests = DBtests(this, null)
        viewFinder = findViewById(R.id.viewFinder)
        val next_button: Button = findViewById(R.id.next_button)
        val prev_button: Button = findViewById(R.id.prev_button)
        student_names = intent.getStringArrayListExtra("students")!!
        aruco_ids = intent.getIntegerArrayListExtra("aruco_ids")!!
        questionText = findViewById(R.id.questionText)

        val resultsButton: Button = findViewById(R.id.results_button)

        val testId = intent.getStringExtra("test_id") ?: ""
        val allTestsMode = intent.getBooleanExtra("allTests", false)
        val quiz_id = intent.getIntExtra("quiz_id", -1)
        val class_id = intent.getIntExtra("class_id", -1)
        val takenSurveyId = intent.getIntExtra("taken_survey_id", -1)
        val takenQuestionId = intent.getIntExtra("taken_question_id", -1)
        val initial_question = intent.getStringExtra("question_text").toString()
        currentQuestionData?.taken_question_id = takenQuestionId
        currentQuestionData?.taken_survey_id = takenSurveyId
        lifecycleScope.launch {
            val classInfoList = fetchClassInfo(class_id)
            arucoToStudentMap = classInfoList.associate { it.aruco_num to it.id }
        }
        val initialQuestionText = intent.getStringExtra("question_text") ?: ""

        // Инициализируем первый вопрос
        currentQuestionData = QuestionData(
            status = "next_question",
            taken_question_id = takenQuestionId,
            taken_survey_id = takenSurveyId,
            question_text = initialQuestionText
        )

        // Загружаем правильный ответ для первого вопроса из БД
        lifecycleScope.launch {
            loadCurrentQuestionAnswer()
            updateQuestionUI(currentQuestionData!!)
        }
        val answers = mutableListOf<Answer>()

        loadQuestionsFromDatabase(allTestsMode, testId)

        showCurrentQuestion()
        next_button.setOnClickListener {
            lifecycleScope.launch {
                println(currentQuestionData)
                overlayView.removeCircles()
                handleNextQuestion(takenSurveyId)
            }
        }

        resultsButton.setOnClickListener {
            lifecycleScope.launch {
                stopSession(quiz_id, class_id)
                navigateToResults(allTestsMode, testId)
            }
        }



        prev_button.setOnClickListener {
            showPreviousQuestion()
        }
    }
    private suspend fun handleNextQuestion(takenSurveyId: Int) {
        val answers = collectAnswers()
        println("Collected answers: $answers")

        try {
            val response = sendResults(
                takenSurveyId,
                currentQuestionData?.taken_question_id ?: -1,
                answers
            )

            when (response.data.status) {
                "next_question" -> {
                    currentQuestionData = response.data
                    updateQuestionUI(response.data)
                    loadCurrentQuestionAnswer()
                }
                "survey_completed" -> {
                    navigateToResults()
                }
                else -> {
                    Toast.makeText(this, "Неизвестный статус: ${response.data.status}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Вопросы закончились, проверьте результаты!", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
    private fun loadCurrentQuestionAnswer() {
        currentQuestionData?.let { questionData ->
            questionData.question_id?.let { questionId ->
                val cursor = db_tests.getQuestionById(questionId)
                if (cursor.moveToFirst()) {
                    updateRightAnswers(cursor)
                } else {
                    questionData.question_text?.let { questionText ->
                        val textCursor = db_tests.getQuestionByText(questionText)
                        if (textCursor.moveToFirst()) {
                            updateRightAnswers(textCursor)
                            questionData.question_id = textCursor.getInt(3)
                        }
                    }
                }
            } ?: run {
                // Если question_id нет, ищем по тексту вопроса
                questionData.question_text?.let { questionText ->
                    val cursor = db_tests.getQuestionByText(questionText)
                    if (cursor.moveToFirst()) {
                        updateRightAnswers(cursor)
                        questionData.question_id = cursor.getInt(3)
                    }
                }
            }
        }
    }

    private fun updateRightAnswers(cursor: Cursor) {
        right_answers.clear()
        val answer = cursor.getString(2)
        right_answers.add(answer)
    }
    private fun collectAnswers(): List<Answer> {
        val answers = mutableListOf<Answer>()
        for ((arucoIdStr, answerStr) in id_map) {
            try {
                val arucoNum = arucoIdStr.toInt()
                val answer = answerStr.toInt()
                val studentId = arucoToStudentMap[arucoNum]

                studentId?.let {
                    answers.add(Answer(
                        student_id = it,
                        answer = answer
                    ))
                }
            } catch (e: NumberFormatException) {
                continue
            }
        }
        return answers
    }

    private fun updateQuestionUI(questionData: QuestionData) {

        questionData.question_text?.let {
            questionText.text = it
            questionList.add(it)
        }

        // Очищаем текущие ответы студентов
        id_map.clear()

        // Обновляем ID текущего вопроса
        questionData.taken_question_id?.let {
            question_ids.add(it)
        }
    }

    private fun showPreviousQuestion() {
        if (questionNum > 0) {
            questionNum--
            showCurrentQuestion()
        }
    }

    private fun loadQuestionsFromDatabase(allTestsMode: Boolean, testId: String) {
        if (allTestsMode) {
            val cursor = db_tests.getAllQuestions()
            while (cursor.moveToNext()) {
                questionList.add(cursor.getString(1)) // question_text
                right_answers.add(cursor.getString(2))
                question_ids.add(cursor.getInt(3))// right_answer
            }
            cursor.close()
        } else {
            val cursor = db_tests.getTestQuestions(testId)
            while (cursor.moveToNext()) {
                questionList.add(cursor.getString(1)) // question_text
                right_answers.add(cursor.getString(2))
                question_ids.add(cursor.getInt(3))// right_answer
            }
            cursor.close()
        }
    }

    private fun showCurrentQuestion() {
        println(questionNum)
        if (questionList.isNotEmpty() && questionNum < questionList.size && questionNum > 0) {
            println(questionList[questionNum])
            questionText.text = questionList[questionNum]
        }
    }


    private fun navigateToResults(allTestsMode: Boolean = false, testId: String = "") {
        val intent = Intent(this, ResultsActivity::class.java).apply {
                putStringArrayListExtra("questions", ArrayList(questionList))
                bundle.putStringArrayList("keys", ArrayList(id_map.keys))
                bundle.putStringArrayList("values", ArrayList(id_map.values.map { it.toString() }))
                bundle.putIntegerArrayList("aruco_id", aruco_ids)
                bundle.putStringArrayList("student_name", student_names)
            }


        startActivity(intent)
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
                cameraProvider.unbindAll()
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
        private var text_to_put: String = ""



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
            overlayView = findViewById(R.id.overlayView)
            overlayView.invalidate()

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
                            text_to_put = "0"
                        } else if (topLeftX > bottomRightX && topLeftY < bottomRightY) {
                            text_to_put = "1"
                        } else if (topLeftX > bottomRightX && topLeftY > bottomRightY) {
                            text_to_put = "2"
                        } else if (topLeftX < bottomRightX && topLeftY > bottomRightY) {
                            text_to_put = "3"
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

    private suspend fun sendResults(
        taken_survey_id: Int,
        taken_question_id: Int,
        answers: List<Answer>
    ): QuestionResponse {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        return try {
            println(taken_question_id)
            val response = client.post("https://eduvision.na4u.ru/api/api/conducting/answers") {
                contentType(ContentType.Application.Json)
                setBody(QuestionResultsRequest(taken_survey_id, taken_question_id, answers))
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            // Парсим ответ сервера
            val responseText = response.bodyAsText()
            println("Server response: $responseText")

            Json.decodeFromString<QuestionResponse>(responseText)
        } finally {
            client.close()
        }
    }
    suspend fun stopSession(quiz_id: Int, class_id: Int) {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.post("https://eduvision.na4u.ru/api/api/conducting/stop") {
                contentType(ContentType.Application.Json)
                setBody(SessionInfo(quiz_id, class_id))
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")

                }

            }

        } finally {
            client.close()
        }

    }

    private suspend fun fetchClassInfo(class_id: Int): List<ClassInfo> {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.get("https://eduvision.na4u.ru/api/api/conducting/students?class_id=$class_id") {
              //  contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
            val apiResponse = response.body<ApiResponse>()

            return apiResponse.data
        } finally {
            client.close()
        }

    }

}
