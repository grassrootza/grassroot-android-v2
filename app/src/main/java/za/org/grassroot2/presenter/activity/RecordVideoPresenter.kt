package za.org.grassroot2.presenter.activity

import za.org.grassroot2.util.MediaRecorderWrapper
import za.org.grassroot2.view.GrassrootView
import javax.inject.Inject

class RecordVideoPresenter @Inject constructor(val mediaRecorderWrapper: MediaRecorderWrapper) : BasePresenter<RecordVideoPresenter.RecordVideoView>() {

    fun startRecording() {
        mediaRecorderWrapper.initAndStartRecording()
        view.startAnimation()
    }

    fun cancelRecording() {
        mediaRecorderWrapper.cancelRecording()
        view.stopAnimation()
    }

    interface RecordVideoView : GrassrootView {
        fun startAnimation()
        fun stopAnimation()
    }

    fun completeRecording() : String? = mediaRecorderWrapper.stopRecoring()
}
