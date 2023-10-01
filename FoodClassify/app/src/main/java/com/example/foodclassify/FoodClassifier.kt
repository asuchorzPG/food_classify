package com.example.foodclassify

import android.content.Context
import android.graphics.*
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.foodclassify.ml.FlowerModel
import org.tensorflow.lite.support.image.TensorImage
import java.io.ByteArrayOutputStream

data class ClassificationResult(val label:String, val score:Float)

typealias ClassificationListener = (result: ClassificationResult) -> Unit

class FoodClassifier (ctx: Context, private val listener:ClassificationListener) : ImageAnalysis.Analyzer{

    val fmodel = FlowerModel.newInstance(ctx)

    override fun analyze(image: ImageProxy) {
        val bitmap = image.toBitmap()
        val tensorImg = TensorImage.fromBitmap(bitmap)

        val outputs = fmodel.process(tensorImg).probabilityAsCategoryList.apply { sortByDescending { it.score } }.take(3)
        listener(ClassificationResult(outputs[0].label, outputs[0].score))
        image.close()
    }

    fun ImageProxy.toBitmap(): Bitmap {
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
}