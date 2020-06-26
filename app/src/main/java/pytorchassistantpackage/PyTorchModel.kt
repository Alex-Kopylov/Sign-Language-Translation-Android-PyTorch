package pytorchassistantpackage

import android.content.Context
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


open class Classifier(context: Context, moduleName: String) {
   private val module: Module = setModule(context, moduleName)

   private fun setModule(context: Context, moduleName: String): Module {
      return ModuleHandler(context, moduleName).getModule()
   }

   fun predict(X: Tensor) = module.forward(IValue.from(X as Tensor)).toTensor()!!
}

class ModuleHandler(private val context: Context, private val moduleName: String){
   private val module = loadModule()

   private fun getAssetAbsolutePathByName(context: Context, assetName: String): String? {
      val file = File(context.filesDir, assetName)
      try {
         context.assets.open(assetName).use { `is` ->
            FileOutputStream(file).use { os ->
               val buffer = ByteArray(4 * 1024)
               var read: Int
               while (`is`.read(buffer).also { read = it } != -1) {
                  os.write(buffer, 0, read)
               }
               os.flush()
            }
            return file.absolutePath
         }
      } catch (e: IOException) {
         Log.e("Error: loading model", "Error process asset $assetName to file path")
      }
      return null
   }

   private fun loadModule(): Module {
      val modelPath =
         getAssetAbsolutePathByName(context, moduleName)
      return Module.load(modelPath)
   }

   fun getModule() = module
}