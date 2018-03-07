package za.org.grassroot2.view.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.View
import android.widget.EditText
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_vote_details.*
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Post
import za.org.grassroot2.model.task.Vote
import za.org.grassroot2.model.task.VoteResult
import za.org.grassroot2.presenter.activity.VoteDetailsPresenter
import za.org.grassroot2.view.adapter.PostAdapter
import za.org.grassroot2.view.adapter.VoteResultsAdapter
import javax.inject.Inject

class VoteDetailsActivity : GrassrootActivity(), VoteDetailsPresenter.VoteDetailsView {

    private var voteUid: String? = null

    @Inject lateinit var presenter: VoteDetailsPresenter
    @Inject lateinit var rxPermissions: RxPermissions
    @Inject lateinit var resultsAdapter: VoteResultsAdapter
    @Inject lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // handle if coming in here from notification (means may need to fetch vote)
        val triggeredByNotification = intent.getBooleanExtra(TRIGGERED_BY_NOTIFICATION, false)
        voteUid = intent.getStringExtra(EXTRA_VOTE_UID)

        // standard view creation
        initView()
        presenter.attach(this)
        presenter.init(voteUid!!, triggeredByNotification)

        //add vote results
        vote_results.adapter = resultsAdapter
        vote_results.layoutManager = LinearLayoutManager(this)

        // handle posts
        writePostButton.setOnClickListener { writePost() }
        posts.adapter = postAdapter
        posts.layoutManager = LinearLayoutManager(this)
    }

    private fun writePost() {
        CreatePostActivity.start(this, presenter.vote.uid, presenter.vote.parentUid)
    }

    override fun onResume() {
        super.onResume()
        presenter.loadData()
    }

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    override fun getLayoutResourceId(): Int = R.layout.activity_vote_details

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    private fun initView() {
        initToolbar()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        toolbar.title = ""
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp)
        toolbar.setNavigationOnClickListener { v -> finish() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_group_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun render(vote: Vote) {
        voteSubject.text = vote.name
        renderVoteOptions(vote)
        renderTally(vote)
    }

    private fun renderVoteOptions(vote: Vote) {
        voteSelectOption.setOnClickListener({ displayAlert(vote) });
    }

    fun displayAlert(vote: Vote) {
        val alert = AlertDialog.Builder(this)
        var voteChoice: EditText? = null
        var result: String

        var options: MutableSet<String> = vote.voteOptions.keys

//        val cs: CharSequence = options.elementAt(0)
//            val itemsArray = arrayOfNulls<CharSequence>(options.size)
//        val arrayOption = options.toTypedArray();
//        options.forEachIndexed((index, option) -> itemsArray[index] = option);

//        val items = arrayOf<CharSequence>(options.elementAt(0), options.elementAt(1), options.elementAt(2))

//        val itemOptions: Array<CharSequence> = vote.voteOptions.keys.;

        //var displayOptions: Array<CharSequence> = List<Map.Entry<String, Int>>(vote.voteOptions.map({ options -> options }), bla)
        // Builder
        with(alert) {
            setTitle("Vote Options")
                    .setItems(vote.voteOptions.keys.toTypedArray(), DialogInterface.OnClickListener { dialog, which ->
                        // The 'which' argument contains the index position
                        // of the selected item
                        Timber.d("User selected option: %s", options.elementAt(which))
                        presenter.respondToVote(vote.uid, options.elementAt(which))
                    })
        }
        // Dialog
        val dialog = alert.create()
        dialog.setView(voteChoice)
        dialog.show()
    }



    private fun renderTally(vote: Vote) {
        Timber.d("rendering these options: %s", vote.voteOptions?.toString())
        resultsAdapter.setData(convertVoteResults(vote));
    }


    private fun convertVoteResults(vote: Vote): List<VoteResult> {
        if (vote.voteOptions == null) {
            val list = listOf<VoteResult>()
            return list
        } else {
            val totalVotes = vote.voteOptions.values.sum();
            return vote.voteOptions.map { entry -> VoteResult(entry.key, entry.value, entry.value.toDouble() / totalVotes) }
        }
    }

    override fun renderPosts(posts: List<Post>) {
        if (posts.isNotEmpty()) {
            listTitle.visibility = View.VISIBLE
            postAdapter.setData(posts)
        }
    }

    companion object {

        val EXTRA_VOTE_UID = "vote_uid"
        val TRIGGERED_BY_NOTIFICATION = "triggered_by_notification"

        fun start(activity: Activity, meetingUid: String) {
            val intent = Intent(activity, VoteDetailsActivity::class.java)
            intent.putExtra(EXTRA_VOTE_UID, meetingUid)
            activity.startActivity(intent)
        }
    }
}