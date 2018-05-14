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

class CreateMeetingActivity : GrassrootActivity() {

    @Inject
    lateinit var presenter: CreateActionPresenter
    @Inject
    lateinit var rxPermissions: RxPermissions // inspect

    override fun getLayoutResourceId(): Int = R.layout.activity_create_meeting

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var meetingName = findViewById(R.id.et_create_meeting_name) as EditText
        var meetingDescription = findViewById(R.id.et_create_meeting_description) as EditText

        var button = findViewById(R.id.bt_create_meeting) as Button

        button.setOnClickListener {
            fun onClick() {
                var name = meetingName.text.toString()
                var description = meetingDescription.text.toString()
                presenter.createMeeting(name, description)
            }
        }
    }

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    companion object {

        fun start(activity: Activity) {
            val intent = Intent(activity, CreateMeetingActivity::class.java)
            activity.startActivity(intent)
        }
    }
}
