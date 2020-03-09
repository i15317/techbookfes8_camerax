package jp.digital_future.cameraxsample.repository

import androidx.camera.core.AspectRatio
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraRepository @Inject constructor(
    private val outputDirectory: File
) {

    private val TAG = "CameraXBasic"
    private val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    private val PHOTO_EXTENSION = ".jpg"
    private val RATIO_4_3_VALUE = 4.0 / 3.0
    private val RATIO_16_9_VALUE = 16.0 / 9.0

    fun createFile() =
        File(
            outputDirectory, SimpleDateFormat(FILENAME, Locale.US)
                .format(System.currentTimeMillis()) + PHOTO_EXTENSION
        )


    fun getFileLists(): Array<File>? = outputDirectory.listFiles()

    fun aspectRatio(width: Int, height: Int): AspectRatio {
        val previewRatio = max(width, height).toDouble() / min(width, height)

        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

}
