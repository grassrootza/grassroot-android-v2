package za.org.grassroot2.presenter

import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.services.rest.RestResponse
import za.org.grassroot2.view.GrassrootView
import javax.inject.Inject


class MeetingDetailsPresenter @Inject
constructor(private val databaseService: DatabaseService, private val networkService: NetworkService) : BasePresenter<MeetingDetailsPresenter.MeetingDetailsView>() {
    private var meetingUid: String? = null

    fun init(meetingUid: String) {
        this.meetingUid = meetingUid
    }

    lateinit var meeting: Meeting
        private set


    fun loadData() {
        disposableOnDetach(databaseService.load(Meeting::class.java, meetingUid!!).subscribeOn(io()).observeOn(main()).subscribe({ meeting ->
            view.render(meeting)
            this.meeting = meeting
        }, { it.printStackTrace() }))
    }


    interface MeetingDetailsView : GrassrootView {
        fun render(meeting: Meeting)
    }

    fun respondToMeeting(uid: String, response: String) {
        view.showProgressBar()
        //dummy error response is returned in case of any failure, and in that case item is marked for sync
        disposableOnDetach(networkService.respondToMeeting(uid, response).subscribeOn(io()).observeOn(main()).onErrorReturn { RestResponse.errorResponse() }.subscribe({ networkResponse ->
            view.closeProgressBar()
                disposableOnDetach(databaseService.load(Meeting::class.javaObjectType, uid).flatMapSingle { meeting ->
                    meeting.response = response
                    meeting.isSynced = networkResponse.isSuccessful
                    return@flatMapSingle databaseService.store(Meeting::class.javaObjectType, meeting)
                }.observeOn(main()).subscribe({ t -> view.render(t) }))
        }, { _ -> view.closeProgressBar() }))
    }

}
