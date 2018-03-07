package za.org.grassroot2.presenter.activity

import timber.log.Timber
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Post
import za.org.grassroot2.model.task.Vote
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.view.GrassrootView
import javax.inject.Inject

/**
 * Created by luke on 2017/12/18.
 */
class VoteDetailsPresenter @Inject
constructor(private val databaseService: DatabaseService, private val networkService: NetworkService): BasePresenter<VoteDetailsPresenter.VoteDetailsView>() {

    private lateinit var voteUid: String
    private var forceSync = false

    fun init(voteUid: String, forceSync: Boolean) {
        this.voteUid = voteUid
        this.forceSync = forceSync
    }

    lateinit var vote: Vote
        private set

    fun loadData() {
        if (forceSync) {
            view.showProgressBar()
            val voteUidAndType = mapOf(voteUid to "VOTE")
            disposableOnDetach(networkService.getTasksByUids(voteUidAndType)
                    .subscribeOn(io()).observeOn(main()).subscribe(
                    { tasksFetched ->
                        view.closeProgressBar()
                        databaseService.storeTasks(tasksFetched)
                        displayData()
                    },
                    { throwable ->
                        view.closeProgressBar()
                        Timber.e(throwable)
                        displayData()
                    }
            ))

        } else displayData()
    }

    private fun displayData() {
        disposableOnDetach(databaseService.load(Vote::class.java, voteUid).subscribeOn(io()).observeOn(main()).subscribe({ vote ->
            this.vote = vote;
            view.render(vote);
        }, { Timber.e(it) }))
    }

    fun respondToVote(uid: String, response: String) {
        view.showProgressBar()
        disposableOnDetach(networkService.respondToVote(uid, response).subscribeOn(io()).observeOn(main()).subscribe({ networkResponse ->
            view.closeProgressBar()
            disposableOnDetach(databaseService.load(Vote::class.javaObjectType, uid).flatMapSingle { vote ->
                vote.voteResponse = response
                return@flatMapSingle databaseService.store(Vote::class.javaObjectType, vote)
            }.observeOn(main()).subscribe({ t -> view.render(t) }))
        }, {throwable ->
            // todo : handle the various types of errors
            view.closeProgressBar()
        }));
    }

    interface VoteDetailsView : GrassrootView {
        fun render(vote: Vote)
        fun renderPosts(posts: List<Post>)
    }

}