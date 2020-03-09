package jp.digital_future.cameraxsample.ui.camera

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import androidx.camera.core.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.observe
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import com.android.example.cameraxbasic.utils.ANIMATION_FAST_MILLIS
import com.android.example.cameraxbasic.utils.ANIMATION_SLOW_MILLIS
import com.android.example.cameraxbasic.utils.simulateClick
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dagger.Module
import dagger.Provides
import dagger.android.support.AndroidSupportInjection
import jp.digital_future.cameraxsample.MainActivity
import jp.digital_future.cameraxsample.R
import jp.digital_future.cameraxsample.actioncreator.CameraActionCreator
import jp.digital_future.cameraxsample.databinding.FragmentCameraBinding
import jp.digital_future.cameraxsample.di.PageScope

import jp.digital_future.cameraxsample.ui.AutoFitPreviewBuilder
import jp.digital_future.cameraxsample.ui.PermissionsFragment
import jp.digital_future.cameraxsample.util.CustomAnalyzer
import jp.digital_future.cameraxsample.viewmodel.CameraViewModel
import me.tatarka.injectedvmprovider.InjectedViewModelProvider
import java.io.File
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Provider

const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"
const val IMMERSIVE_FLAG_TIMEOUT = 500L

class CameraFragment : Fragment() {

    @Inject
    lateinit var actionCreator: CameraActionCreator

    @Inject
    lateinit var viewModelProvider: Provider<CameraViewModel>
    private val store: CameraViewModel by lazy {
        InjectedViewModelProvider.of(requireActivity()).get(viewModelProvider)
    }
    private lateinit var viewFinder: TextureView
    private lateinit var container: ConstraintLayout
    private lateinit var mainExecutor: Executor
    private lateinit var broadcastManager: LocalBroadcastManager

    //カメラ関連のリソース定義
    //ディスプレイ番号
    private var displayId = -1

    //表示用オブジェクト
    private lateinit var preview: Preview

    //画像の保存を行うオブジェクト？
    private lateinit var imageCapture: ImageCapture

    //画像解析用オブジェクト
    private lateinit var imageAnalyzer: ImageAnalysis

    //仮想ディスプレイを管理する（？）
    private lateinit var displayManager: DisplayManager

    private val volumeDownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)) {
                // When the volume down button is pressed, simulate a shutter button click
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    val shutter = container
                        .findViewById<ImageButton>(R.id.camera_capture_button)
                    shutter.simulateClick()
                }
            }
        }
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraFragment.displayId) {
                preview.setTargetRotation(view.display.rotation)
                imageCapture.setTargetRotation(view.display.rotation)
                imageAnalyzer.setTargetRotation(view.display.rotation)
            }
        } ?: Unit
    }

    private val imageSavedListener = object : ImageCapture.OnImageSavedListener {
        override fun onError(
            error: ImageCapture.ImageCaptureError, message: String, exc: Throwable?
        ) {
            Log.e(TAG, "Photo capture failed: $message")
            exc?.printStackTrace()
        }

        override fun onImageSaved(photoFile: File) {
            actionCreator.updateThumbnali(photoFile)

            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(photoFile.extension)
            MediaScannerConnection.scanFile(
                context, arrayOf(photoFile.absolutePath), arrayOf(mimeType), null
            )
        }
    }

    @SuppressLint("RestrictedApi")
    private fun setupObserveData() {
        store.thumbnailFile.observe(this) {
            setGalleryThumbnail(it)
        }

        store.outputFile.observe(this) { file ->
            imageCapture.let {
                val metadata = ImageCapture.Metadata().apply {
                    // Mirror image when using the front camera
                    isReversedHorizontal = (store.lensFacing.value == CameraX.LensFacing.FRONT)
                }

                // Setup image capture listener which is triggered after photo has been taken
                imageCapture.takePicture(file, metadata, mainExecutor, imageSavedListener)

                // Display flash animation to indicate that photo was captured
                container.postDelayed({
                    container.foreground = ColorDrawable(Color.WHITE)
                    container.postDelayed(
                        { container.foreground = null }, ANIMATION_FAST_MILLIS
                    )
                }, ANIMATION_SLOW_MILLIS)
            }
        }
        store.screenAspectRatio.observe(this) {
            CameraX.unbindAll()
            bindCameraUseCases(it)
        }

        store.lensFacing.observe(this) { lensFacing ->
            try {
                // Only bind use cases if we can query a camera with this orientation
                CameraX.getCameraWithLensFacing(lensFacing)

                // Unbind all use cases and bind them again with the new lens facing configuration
                CameraX.unbindAll()
                actionCreator.calcScreenAspectRatio(DisplayMetrics().also {
                    viewFinder.display.getRealMetrics(
                        it
                    )
                })
            } catch (exc: Exception) {
                // Do nothing
            }
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainExecutor = ContextCompat.getMainExecutor(requireContext())
    }

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since user could have removed them
        //  while the app was on paused state
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.navigation_camera).navigate(
                CameraFragmentDirections.actionCameraToPermissions()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unregister the broadcast receivers and listeners
        broadcastManager.unregisterReceiver(volumeDownReceiver)
        displayManager.unregisterDisplayListener(displayListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCameraBinding.inflate(
            inflater,
            container,
            false
        )

        this.container = binding.cameraContainer
        return binding.root

    }

    private fun setGalleryThumbnail(file: File) {

        val thumbnail = container.findViewById<ImageButton>(R.id.photo_view_button) ?: return
        // Run the operations in the view's thread
        thumbnail.post {
            // Remove thumbnail padding
            thumbnail.setPadding(resources.getDimension(R.dimen.stroke_small).toInt())

            // Load thumbnail into circular button using Glide
            Glide.with(thumbnail)
                .load(file)
                .apply(RequestOptions.circleCropTransform())
                .into(thumbnail)
        }
    }


    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewFinder = container.findViewById(R.id.view_finder)
        broadcastManager = LocalBroadcastManager.getInstance(view.context)

        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
        broadcastManager.registerReceiver(volumeDownReceiver, filter)

        displayManager = viewFinder.context
            .getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(displayListener, null)

        viewFinder.post {
            displayId = viewFinder.display.displayId
            updateCameraUi()
            actionCreator.calcScreenAspectRatio(DisplayMetrics().also {
                viewFinder.display.getRealMetrics(
                    it
                )
            })
        }
        setupObserveData()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateCameraUi()
    }

    private fun bindCameraUseCases(screenAspectRatio: AspectRatio) {
        val lensFacing: CameraX.LensFacing =
            if (store.lensFacing.value != null) store.lensFacing.value!! else CameraX.LensFacing.BACK

        val viewFinderConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(viewFinder.display.rotation)
        }.build()

        // Set up the capture use case to allow users to take photos
        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setLensFacing(lensFacing)
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(viewFinder.display.rotation)
        }.build()

        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            setLensFacing(lensFacing)
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            setTargetRotation(viewFinder.display.rotation)
        }.build()

        // Use the auto-fit preview builder to automatically handle size and orientation changes
        preview = AutoFitPreviewBuilder.build(viewFinderConfig, viewFinder)

        imageCapture = ImageCapture(imageCaptureConfig)

        imageAnalyzer = ImageAnalysis(analyzerConfig).apply {
            setAnalyzer(mainExecutor,
                CustomAnalyzer { luma ->

                })
        }
        CameraX.bindToLifecycle(
            viewLifecycleOwner, preview, imageCapture, imageAnalyzer
        )
    }

    @SuppressLint("RestrictedApi")
    private fun updateCameraUi() {

        container.findViewById<ConstraintLayout>(R.id.camera_ui_container)?.let {
            container.removeView(it)
        }

        val controls = View.inflate(requireContext(), R.layout.camera_ui_container, container)

        controls.findViewById<ImageButton>(R.id.camera_capture_button).setOnClickListener {
            actionCreator.updateSaveFileName()
        }

        // Listener for button used to switch cameras
        controls.findViewById<ImageButton>(R.id.camera_switch_button).setOnClickListener {
            actionCreator.updateLensFacing()
        }

        // Listener for button used to view last photo
        controls.findViewById<ImageButton>(R.id.photo_view_button).setOnClickListener {
            CameraX.unbindAll()
            actionCreator.navigateCameraToGallery(this)
        }
    }
}

@Module
internal abstract class CameraFragmentModule {
    @Module
    companion object {
        @JvmStatic
        @Provides
        @PageScope
        fun providesLifecycle(cameraFragment: CameraFragment): Lifecycle = cameraFragment.lifecycle

        @JvmStatic
        @Provides
        @PageScope
        fun provideOutputCameraDirectory(cameraFragment: CameraFragment): File =
            MainActivity.getOutputDirectory(cameraFragment.requireContext())
    }
}
