package signlanguagetranslation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.core.app.ActivityCompat

var LANGUAGE = "ASL"

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_PERMISSIONS = 101
    private val REQUIRED_PERMISSIONS = arrayOf("android.permission.CAMERA")

    private lateinit var cameraListener:Camera
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraListener = Camera(this@MainActivity)

        if (allPermissionsGranted()) {
            cameraListener.initializeCamera(lifecycle)
            Classification.setupClassification(this@MainActivity)
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

    }

    override fun onRequestPermissionsResult( //При первом запуске программы
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                cameraListener.initializeCamera(lifecycle)
                Classification.setupClassification(this@MainActivity)
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

}