package jp.digital_future.cameraxsample.dispatcher

import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.FlowableProcessor
import jp.digital_future.cameraxsample.action.CameraAction
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

class CameraDispatcher {

    private val cameraSaveActions = BroadcastChannel<CameraAction>(Channel.CONFLATED)
    val receiveChannel: ReceiveChannel<CameraAction> = cameraSaveActions.openSubscription()
    fun launchAndDispatch(action: CameraAction) {
        GlobalScope.launch {
            cameraSaveActions.send(action)
        }
    }

    private val dispatcherUpdateThumbnail: FlowableProcessor<CameraAction.CameraThumbnailAction> =
        BehaviorProcessor.create<CameraAction.CameraThumbnailAction>().toSerialized()

    private val dispatcherUpdateFilename: FlowableProcessor<CameraAction.CamaraUpdateOutputFile> =
        BehaviorProcessor.create<CameraAction.CamaraUpdateOutputFile>().toSerialized()

    private val dispatcherCalcScreenAspectsRatio: FlowableProcessor<CameraAction.CalcScreenAspectsRatio> =
        BehaviorProcessor.create<CameraAction.CalcScreenAspectsRatio>().toSerialized()

    private val dispatcherUpdateLensFacing: FlowableProcessor<CameraAction.ActionUpdateLensfacing> =
        BehaviorProcessor.create<CameraAction.ActionUpdateLensfacing>().toSerialized()

    val onUpdateThumbnail: Flowable<CameraAction.CameraThumbnailAction> = dispatcherUpdateThumbnail

    val onUpdateFilename: Flowable<CameraAction.CamaraUpdateOutputFile> = dispatcherUpdateFilename

    val onCalcScreenAspectsRatio: Flowable<CameraAction.CalcScreenAspectsRatio> =
        dispatcherCalcScreenAspectsRatio

    val onUpdateLensfacing: Flowable<CameraAction.ActionUpdateLensfacing> =
        dispatcherUpdateLensFacing

    fun dispatch(cameraAction: CameraAction.CameraThumbnailAction) {
        dispatcherUpdateThumbnail.onNext(cameraAction)
    }

    fun dispatch(action: CameraAction.CamaraUpdateOutputFile) {
        dispatcherUpdateFilename.onNext(action)
    }

    fun dispatch(action: CameraAction.CalcScreenAspectsRatio) {
        dispatcherCalcScreenAspectsRatio.onNext(action)
    }

    fun dispatch(action: CameraAction.ActionUpdateLensfacing) {
        dispatcherUpdateLensFacing.onNext(action)
    }

}