package pytorchassistantpackage.vision

import pytorchassistantpackage.Classifier
import android.content.Context
import android.graphics.*
import androidx.camera.core.ImageProxy
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.ByteArrayOutputStream


class ImageClassifier(context: Context, moduleName: String = "ClfModel.pt", private val arrayOfClasses:ArrayList<String>):Classifier(context, moduleName) {

    fun processImage(image:ImageProxy): String {
        val bitmapTensorNormalized = imageNormalization(image.toBitmap())
        val outputTensor = predict(bitmapTensorNormalized!!)
        val scores = outputTensor.dataAsFloatArray
        val maxScoreIdx = scores.indices.maxBy { scores[it] } ?: -1
        return arrayOfClasses[maxScoreIdx]

    }

    private fun ImageProxy.toBitmap(): Bitmap { //Конвертер ImageProxy(класс для работы с камерой в Android) в bitmap
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun imageNormalization(image: Bitmap, mode: String = "ImageNet", stats:Array<FloatArray> = emptyArray()): Tensor? {
        return when (mode) {
            "ImageNet" -> TensorImageUtils.bitmapToFloat32Tensor(
                image,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB
            )
            else -> TensorImageUtils.bitmapToFloat32Tensor(
                image,
                stats[0],
                stats[1]
            )
        }
    }
}
