package za.org.grassroot2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_group_settings.*
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Group
import za.org.grassroot2.presenter.activity.GroupSettingsPresenter
import za.org.grassroot2.view.activity.GrassrootActivity
import javax.inject.Inject

class GroupSettingsActivity : GrassrootActivity(), GroupSettingsPresenter.GroupSettingsView {

    @Inject lateinit var presenter: GroupSettingsPresenter
    private var groupUid: String? = null

    override fun getLayoutResourceId(): Int = R.layout.activity_group_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupUid = intent.getStringExtra(EXTRA_GROUP_UID)
        presenter.attach(this)
        presenter.init(groupUid!!)
        initToolbar()
    }

    override fun onResume() {
        super.onResume()
        presenter.loadData()
    }

    override fun render(group: Group) {
        supportActionBar!!.setTitle(group.name)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp)
        toolbar.setNavigationOnClickListener { v -> finish() }
    }

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }


    companion object {
        private val EXTRA_GROUP_UID = "group_uid";

        fun start(activity: Activity, groupUid: String?) {
            val intent = Intent(activity, GroupSettingsActivity::class.java)
            intent.putExtra(EXTRA_GROUP_UID, groupUid)
            activity.startActivity(intent)
        }
    }
}
