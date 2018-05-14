package za.org.grassroot2.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.tbruyelle.rxpermissions2.RxPermissions
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.presenter.activity.CreateActionPresenter
import javax.inject.Inject

class CreateVoteActivity : GrassrootActivity() {

    @Inject
    lateinit var presenter: CreateActionPresenter
    @Inject
    lateinit var rxPermissions: RxPermissions // inspect

    override fun getLayoutResourceId(): Int = R.layout.activity_create_vote

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var voteName = findViewById(R.id.et_create_vote_name) as EditText
        var voteDescription = findViewById(R.id.et_create_vote_description) as EditText

        var button = findViewById(R.id.bt_create_group) as Button // TODO: switch back to vote button after debug

        button.setOnClickListener {
            fun onClick(which: Int) {
                var name = voteName.text.toString()
                var description = voteDescription.text.toString()
                presenter.createMeeting(name, description)
            }
        }
    }

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }


    companion object {

        fun start(activity: Activity) {
            val intent = Intent(activity, CreateVoteActivity::class.java)
            activity.startActivity(intent)
        }
    }
}
