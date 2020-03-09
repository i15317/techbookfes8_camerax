package jp.digital_future.cameraxsample.actioncreator

import android.util.DisplayMetrics
import androidx.camera.core.CameraX
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import jp.digital_future.cameraxsample.action.CameraAction
import jp.digital_future.cameraxsample.di.PageScope
import jp.digital_future.cameraxsample.dispatcher.CameraDispatcher
import jp.digital_future.cameraxsample.repository.CameraRepository
import jp.digital_future.cameraxsample.ui.EXTENSION_WHITELIST
import jp.digital_future.cameraxsample.ui.camera.CameraFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

val DEFAULT_LENS_FACING = CameraX.LensFacing.BACK


@PageScope
class CameraActionCreator @Inject constructor(
    private val dispatcher: CameraDispatcher,
    private val repository: CameraRepository,
    private val outputDirectory: File,
    @PageScope private val lifecycle: Lifecycle
) : CoroutineScope by lifecycle.coroutineScope {

    private var lensFacing = DEFAULT_LENS_FACING

    init {
        dispatcher.dispatch(CameraAction.ActionUpdateLensfacing(lensFacing))
    }

    fun updateThumbnali(file: File) = dispatcher.dispatch(CameraAction.CameraThumbnailAction(file))

    fun updateSaveFileName() =
        dispatcher.dispatch(CameraAction.CamaraUpdateOutputFile(repository.createFile()))

    // In the background, load latest photo taken (if any) for gallery thumbnail
    fun update() = lifecycle.coroutineScope.launch(Dispatchers.IO) {
        outputDirectory.listFiles { file ->
            EXTENSION_WHITELIST.contains(file.extension.toUpperCase())
        }?.max()?.let { dispatcher.dispatch(CameraAction.CameraThumbnailAction(it)) }
    }

    fun calcScreenAspectRatio(metrics: DisplayMetrics) = dispatcher.dispatch(
        CameraAction.CalcScreenAspectsRatio(
            repository.aspectRatio(
                metrics.widthPixels,
                metrics.heightPixels
            )
        )
    )

    fun navigateCameraToGallery(fragment: Fragment) {
        fragment.findNavController().navigate(
            CameraFragmentDirections.actionCameraToGallery(outputDirectory.absolutePath)
        )
    }

    fun updateLensFacing() {
        lensFacing = if (CameraX.LensFacing.FRONT == lensFacing) {
            CameraX.LensFacing.BACK
        } else {
            CameraX.LensFacing.FRONT
        }
        dispatcher.dispatch(CameraAction.ActionUpdateLensfacing(lensFacing))
    }

}