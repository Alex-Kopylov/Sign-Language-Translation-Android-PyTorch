package signlanguagetranslation

import android.app.Activity
import android.content.Context
import android.graphics.Matrix
import android.util.DisplayMetrics
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import android.widget.TextView
import androidx.camera.core.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry


class Camera(private val context: Context): LifecycleOwner {

    private lateinit var lifecycleRegistry: LifecycleRegistry

    private val textureView = (context as Activity).findViewById<TextureView>(R.id.textureView)
    private val lensFacing: CameraX.LensFacing = CameraX.LensFacing.BACK

    fun initializeCamera(lifecycle: Lifecycle){

        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.markState(Lifecycle.State.STARTED)

        textureView.post { startCamera(lifecycle) }
        textureView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }
    private fun startCamera(lifecycle: Lifecycle) {
        val previewConfig = setPreviewConfig()
        val preview = Preview(previewConfig!!)

        preview.setOnPreviewOutputUpdateListener {
            textureView.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        Classification.bindClfToCameraLifecycle(this as LifecycleOwner, preview)
    }

    private fun setPreviewConfig(): PreviewConfig? {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics().also { textureView.display.getRealMetrics(it) }
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
        val screenAspectRatio = Rational(1, 1)
        return PreviewConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetResolution(screenSize)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(windowManager.defaultDisplay.rotation)
            setTargetRotation(textureView.display.rotation)
        }.build()

    }

    private fun updateTransform() {
        val matrix = Matrix()
        val centerX = textureView.width / 2f
        val centerY = textureView.height / 2f

        val rotationDegrees = when (textureView.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
        textureView.setTransform(matrix)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry

    }


}