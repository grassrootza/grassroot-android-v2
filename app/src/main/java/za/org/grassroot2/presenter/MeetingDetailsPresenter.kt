package za.org.grassroot2.presenter

import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.view.GrassrootView
import javax.inject.Inject


class MeetingDetailsPresenter @Inject
constructor(private val databaseService: DatabaseService, private val networkService: NetworkService) : BasePresenter<MeetingDetailsPresenter.MeetingDetailsView>() {
    private var meetingUid: String? = null

    fun init(meetingUid: String) {
        this.meetingUid = meetingUid
    }

    fun loadData() {
        disposableOnDetach(databaseService.load(Meeting::class.java, meetingUid!!).subscribeOn(io()).observeOn(main()).subscribe({ meeting -> view.render(meeting) }, { it.printStackTrace() }))
    }


    interface MeetingDetailsView : GrassrootView {
        fun render(meeting: Meeting)
        fun emptyData()
    }

    fun respondToMeeting(uid: String, resposne: String) {
        view.showProgressBar()
        disposableOnDetach(networkService.respondToMeeting(uid, resposne).subscribeOn(io()).observeOn(main()).subscribe({ response ->
            view.closeProgressBar()
            if (response.isSuccessful) {
                disposableOnDetach(databaseService.load(Meeting::class.javaObjectType, uid).flatMapSingle { meeting ->
                    return@flatMapSingle databaseService.store(Meeting::class.javaObjectType, meeting)
                }.observeOn(main()).subscribe({ t -> view.render(t) }))
            }
        }, { _ -> view.closeProgressBar() }))
    }

}
