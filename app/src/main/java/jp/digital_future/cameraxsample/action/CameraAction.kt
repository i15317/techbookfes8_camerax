package jp.digital_future.cameraxsample.action

import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraX
import java.io.File


sealed class CameraAction {
    //サムネイル処理のアクション
    data class CameraThumbnailAction(val outputDirectory: File) : CameraAction()

    data class CamaraUpdateOutputFile(val file: File) : CameraAction()

    data class CalcScreenAspectsRatio(val screenAspectsRatio: AspectRatio) : CameraAction()

    data class ActionUpdateLensfacing(val lensFacing: CameraX.LensFacing) : CameraAction()
}
