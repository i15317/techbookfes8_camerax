package jp.digital_future.cameraxsample.viewmodel

import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraX
import androidx.lifecycle.LiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import jp.digital_future.cameraxsample.dispatcher.CameraDispatcher
import jp.digital_future.cameraxsample.flux.Store
import jp.digital_future.cameraxsample.util.ext.toLiveData
import java.io.File
import javax.inject.Inject

class CameraViewModel @Inject constructor(private val dispatcher: CameraDispatcher) : Store() {

    val thumbnailFile: LiveData<File> =
        dispatcher.onUpdateThumbnail.map { it.outputDirectory }
            .observeOn(AndroidSchedulers.mainThread()).toLiveData()

    val outputFile: LiveData<File> =
        dispatcher.onUpdateFilename.map { it.file }.observeOn(AndroidSchedulers.mainThread())
            .toLiveData()

    val screenAspectRatio: LiveData<AspectRatio> =
        dispatcher.onCalcScreenAspectsRatio.map { it.screenAspectsRatio }
            .observeOn(AndroidSchedulers.mainThread()).toLiveData()


    val lensFacing: LiveData<CameraX.LensFacing> =
        dispatcher.onUpdateLensfacing.map { it.lensFacing }
            .observeOn(AndroidSchedulers.mainThread()).toLiveData()

}


