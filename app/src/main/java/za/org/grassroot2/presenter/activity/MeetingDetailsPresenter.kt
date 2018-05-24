package za.org.grassroot2.presenter.activity

import timber.log.Timber
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Post
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.services.rest.RestResponse
import za.org.grassroot2.view.GrassrootView
import javax.inject.Inject


class MeetingDetailsPresenter @Inject
constructor(private val databaseService: DatabaseService, private val networkService: NetworkService) : BasePresenter<MeetingDetailsPresenter.MeetingDetailsView>() {

    private lateinit var meetingUid: String
    private var forceSync = false

    fun init(meetingUid: String, forceSync: Boolean) {
        this.meetingUid = meetingUid
        this.forceSync = forceSync
    }

    lateinit var meeting: Meeting
        private set


    fun loadData() {
        if (forceSync) {
            Timber.d("Showing progress bar 1 in MeetingDetailsPresenter")
            view.showProgressBar()
            val meetingUidAndType = mapOf(meetingUid to "MEETING")
            disposableOnDetach(networkService.getTasksByUids(meetingUidAndType)
                    .subscribeOn(io()).observeOn(main())
                    .subscribe(
                            { tasksFull ->
                                view.closeProgressBar()
                                databaseService.storeTasks(tasksFull)
                                displayData() // data synced with server, display it
                            },
                            { throwable ->
                                view.closeProgressBar()
                                Timber.e(throwable)
                                displayData() // sync failed, display local data anyway
                            }
                    )
            )
        } else displayData()
    }

    private fun displayData() {
        disposableOnDetach(databaseService.load(Meeting::class.java, meetingUid).subscribeOn(io()).observeOn(main()).subscribe({ meeting ->
            this.meeting = meeting
            view.render(this.meeting)
        }, { it.printStackTrace() }))

        disposableOnDetach(networkService.getMeetingPosts(meetingUid!!).subscribeOn(io()).observeOn(main()).subscribe({ resource ->
            view.renderPosts(resource.data!!)
        }, { it.printStackTrace() }))
    }


    interface MeetingDetailsView : GrassrootView {
        fun render(meeting: Meeting)
        fun renderPosts(posts: List<Post>)
    }

    fun respondToMeeting(uid: String, response: String) {
        Timber.d("Showing progress bar 2 in MeetingDetailsPresenter")
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
