package jp.digital_future.cameraxsample.state

enum class CameraState {
    CHANGED,
    FAILED;

    val isChangedSuccess: Boolean get() = this == CameraState.CHANGED
    val isChangedFailure: Boolean get() = this == CameraState.FAILED
}