package signlanguagetranslation

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.widget.TextView
import androidx.camera.core.*
import androidx.lifecycle.LifecycleOwner
import pytorchassistantpackage.vision.ImageClassifier

object Classification {
    private lateinit var analyzerUseCase:ImageAnalysis

    fun setupClassification(context: Context) {
        val predictedTextView =
            (context as Activity).findViewById<TextView>(R.id.predictedTextView)
        //Текстовое поле, куда происходит вывод классификации
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            //Настройка параметров анализатора изображений
            setTargetResolution(Size(224, 224))
            //Размер входного изображения
            val analyzerThread = HandlerThread("AnalysisThread").apply {
                //Поток, в котором классифицируется изображение
                start()
            }
            setCallbackHandler(Handler(analyzerThread.looper))
            // Не дает потоку завершить работу, запуская работу заново
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            // Режим доступа к изображениям с камеры.
            // Поток считывает последнее изображение в очереди
        }.build()

        val imageClf = ImageClassifier(context = context, arrayOfClasses = alphabet()!!)
        //Классификатор изображения
        analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            this.analyzer =
                ImageAnalysis.Analyzer { image: ImageProxy, rotationDegrees: Int ->
                    // Передача изображения и положения экрана для классификации
                    predictedTextView.text = imageClf.processImage(image)
                    // Вывод результата работы нейросети на экран
                }
        }
    }
    fun bindClfToCameraLifecycle(lifecycleOwner: LifecycleOwner, preview: Preview){
        //Связывание потока классификации с жизненным циклом камеры
        CameraX.bindToLifecycle(lifecycleOwner, preview, analyzerUseCase)
    }

    private fun alphabet(): ArrayList<String>? {
        return when (LANGUAGE) {
            "ASL" -> {
                var c: Char = 'A'
                val alphabet: ArrayList<String> = arrayListOf()
                while (c <= 'Z') {
                    alphabet.add(c.toString())
                    ++c
                }
                return alphabet
            }
            //Другие языки...
            else -> null
        }

    }
}
