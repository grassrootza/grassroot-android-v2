package za.org.grassroot2.presenter.activity

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.util.MediaRecorderWrapper
import za.org.grassroot2.view.GrassrootView
import javax.inject.Inject

class RecordAudioPresenter @Inject constructor(val mediaRecorderWrapper: MediaRecorderWrapper, private val networkService: NetworkService) : BasePresenter<RecordAudioPresenter.RecordAudioView>() {

    fun thisIsWhatTheHumanWants(mortalRequest: String) {
        view.showProgressBar()
        disposableOnDetach(networkService.sendNLURequest(mortalRequest).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ NluResponse ->
            Timber.e("NLU request sent")
            Timber.e("This is what I got back %s", NluResponse.toString())
            view.closeProgressBar()

            if (NluResponse.intent.name == "set_meeting") {
                if (NluResponse.entities == null) {
                    Timber.e("The human wants to set a meeting and has gave us no parameters at all. This species takes years off my immortal life.")
                    Timber.d("Initialising create-meeting routine in its entirety")
                    view.initiateCreateAction(R.id.call_meeting)
                }
                else if (NluResponse.entities != null) {
                    Timber.e("Human has met us halfway. Here are available entities: %s", NluResponse.entities.toString())
                }
            }

        }) { throwable ->
            Timber.e("Obstructed: %s", throwable)
            view.closeProgressBar()
        })
    }

    fun startRecording() {
        mediaRecorderWrapper.initAndStartRecording()
        view.startAnimation()
    }

    fun cancelRecording() {
        mediaRecorderWrapper.cancelRecording()
        view.stopAnimation()
    }

    interface RecordAudioView : GrassrootView {
        fun startAnimation()
        fun stopAnimation()
        fun initiateCreateAction(actionToInitiate: Int)
    }

    fun completeRecording() : String? = mediaRecorderWrapper.stopRecoring()
}
