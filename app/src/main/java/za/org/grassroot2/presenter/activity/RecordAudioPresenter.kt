package za.org.grassroot2.presenter.activity

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.model.task.LanguageEntity
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
                    Timber.d("Initialising %s routine in its entirety", NluResponse.intent.name)
                    view.initiateCreateAction(R.id.call_meeting, null)
                }
                else if (NluResponse.entities != null) {
                    Timber.e("Human has met us halfway. Here are available entities: %s", NluResponse.entities.toString())
                    view.initiateCreateAction(R.id.call_meeting, NluResponse.entities)
                }
            }
            else if (NluResponse.intent.name == "call_vote") {
                if (NluResponse.entities == null) {
                    Timber.e("The human wants to set a meeting and has gave us no parameters at all. This species takes years off my immortal life.")
                    Timber.d("Initialising %s routine in its entirety", NluResponse.intent.name)
                    view.initiateCreateAction(R.id.take_vote, null)
                }
                else if (NluResponse.entities != null) {
                    Timber.e("Human has met us halfway. Here are available entities: %s", NluResponse.entities.toString())
                    view.initiateCreateAction(R.id.take_vote, NluResponse.entities)                }
            }
            else if (NluResponse.intent.name == "create_group") {
                if (NluResponse.entities == null) {
                    Timber.e("The human wants to set a meeting and has gave us no parameters at all. This species takes years off my immortal life.")
                    Timber.d("Initialising %s routine in its entirety", NluResponse.intent.name)
                    view.initiateCreateAction(R.id.create_group, null)
                }
                else if (NluResponse.entities != null) {
                    Timber.e("Human has met us halfway. Here are available entities: %s", NluResponse.entities.toString())
                    view.initiateCreateAction(R.id.create_group, NluResponse.entities)                }
            }
            else if (NluResponse.intent.name == "create_info_todo") {
                if (NluResponse.entities == null) {
                    Timber.e("The human wants to set a meeting and has gave us no parameters at all. This species takes years off my immortal life.")
                    Timber.d("Initialising %s routine in its entirety", NluResponse.intent.name)
                    view.initiateCreateAction(R.id.create_todo, null)
                }
                else if (NluResponse.entities != null) {
                    Timber.e("Human has met us halfway. Here are available entities: %s", NluResponse.entities.toString())
                    view.initiateCreateAction(R.id.create_todo, NluResponse.entities)
                }
            }
            else if (NluResponse.intent.name == "create_volunteer_todo") {
                if (NluResponse.entities == null) {
                    Timber.e("The human wants to set a meeting and has gave us no parameters at all. This species takes years off my immortal life.")
                    Timber.d("Initialising %s routine in its entirety", NluResponse.intent.name)
                    view.initiateCreateAction(R.id.create_todo, null)
                }
                else if (NluResponse.entities != null) {
                    Timber.e("Human has met us halfway. Here are available entities: %s", NluResponse.entities.toString())
                    view.initiateCreateAction(R.id.create_todo, NluResponse.entities)
                }
            }
            else if (NluResponse.intent.name == "create_validation_todo") {
                if (NluResponse.entities == null) {
                    Timber.e("The human wants to set a meeting and has gave us no parameters at all. This species takes years off my immortal life.")
                    Timber.d("Initialising %s routine in its entirety", NluResponse.intent.name)
                    view.initiateCreateAction(R.id.create_todo, null)
                }
                else if (NluResponse.entities != null) {
                    Timber.e("Human has met us halfway. Here are available entities: %s", NluResponse.entities.toString())
                    view.initiateCreateAction(R.id.create_todo, NluResponse.entities)
                }
            }
            else if (NluResponse.intent.name == "create_action_todo") {
                if (NluResponse.entities == null) {
                    Timber.e("The human wants to set a meeting and has gave us no parameters at all. This species takes years off my immortal life.")
                    Timber.d("Initialising %s routine in its entirety", NluResponse.intent.name)
                    view.initiateCreateAction(R.id.create_todo, null)
                }
                else if (NluResponse.entities != null) {
                    Timber.e("Human has met us halfway. Here are available entities: %s", NluResponse.entities.toString())
                    view.initiateCreateAction(R.id.create_todo, NluResponse.entities)
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
        fun initiateCreateAction(actionToInitiate: Int, entities: ArrayList<LanguageEntity>?)
    }

    fun completeRecording() : String? = mediaRecorderWrapper.stopRecoring()
}
