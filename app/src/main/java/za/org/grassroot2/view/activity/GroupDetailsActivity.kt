package za.org.grassroot2.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.View
import com.github.florent37.viewtooltip.ViewTooltip
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_group_details.*
import za.org.grassroot2.GroupSettingsActivity
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.extensions.getColorCompat
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.contact.Contact
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.presenter.activity.GroupDetailsPresenter
import za.org.grassroot2.presenter.fragment.GroupFragmentPresenter
import za.org.grassroot2.view.adapter.GenericViewPagerAdapter
import za.org.grassroot2.view.dialog.AddMemberDialog
import za.org.grassroot2.view.fragment.GroupTasksFragment
import java.util.*
import javax.inject.Inject

class GroupDetailsActivity : GrassrootActivity(), GroupDetailsPresenter.GroupDetailsView {

    private var groupUid: String? = null

    @Inject lateinit var presenter: GroupDetailsPresenter
    @Inject lateinit var rxPermissions: RxPermissions
    @Inject lateinit var groupFragmentPresenter:GroupFragmentPresenter

    override val layoutResourceId: Int
        get(): Int = R.layout.activity_group_details

    override fun onInject(component: ActivityComponent) = component.inject(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupUid = intent.getStringExtra(EXTRA_GROUP_UID)
        initView()
        presenter.attach(this)
        presenter.init(groupUid!!)
        fab.setOnClickListener { CreateActionActivity.start(activity, groupUid) }
        inviteMembers.setOnClickListener { displayInviteDialog() }
        about.setOnClickListener { launchGroupSettings() }
    }

    override fun onResume() {
        super.onResume()
        presenter.loadData()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    private fun displayInviteDialog() {
        val df = AddMemberDialog.newInstance(AddMemberDialog.TYPE_PICK)
        df.setAddMemberDialogListener(object : AddMemberDialog.AddMemberDialogListener {
            override fun contactBook() {
                rxPermissions.request(Manifest.permission.READ_CONTACTS).subscribe({ result ->
                    if (result!!) {
                        PickContactActivity.startForResult(this@GroupDetailsActivity, REQUEST_PICK_CONTACTS)
                    }
                }, { it.printStackTrace() })
            }

            override fun manual() {
                showFillDialog()
            }
        })
        df.show(supportFragmentManager, GrassrootActivity.DIALOG_TAG)
    }

    private fun launchGroupSettings() {
        GroupSettingsActivity.start(this, groupUid)
    }

    private fun showFillDialog() {
        val df = AddMemberDialog.newInstance(AddMemberDialog.TYPE_INSERT_MANUAL)
//        df.setContactListener { name: String, phone: String -> presenter.inviteContact(name, phone) }
        df.show(supportFragmentManager, GrassrootActivity.DIALOG_TAG)
    }

    private fun initView() {
        initTabs()
        initToolbar()
    }

    private fun initTabs() {
        val adapter = GenericViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(GroupTasksFragment.newInstance(groupUid, null), getString(R.string.title_all))
        adapter.addFragment(GroupTasksFragment.newInstance(groupUid, GrassrootEntityType.VOTE), getString(R.string.title_votes))
        adapter.addFragment(GroupTasksFragment.newInstance(groupUid, GrassrootEntityType.MEETING), getString(R.string.title_meetings))
        adapter.addFragment(GroupTasksFragment.newInstance(groupUid, GrassrootEntityType.TODO), getString(R.string.title_todos))
        contentPager.adapter = adapter
        tabs!!.setupWithViewPager(contentPager)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp)
        toolbar.setNavigationOnClickListener { v ->
            run {
                groupFragmentPresenter.refreshGroups()
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_group_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_PICK_CONTACTS && resultCode == Activity.RESULT_OK) {
            val contacts = data.getSerializableExtra(PickContactActivity.EXTRA_CONTACTS) as ArrayList<Contact>
            presenter.inviteContacts(contacts)
        }
    }

    override fun render(group: Group) {
        groupTitle.text = group.name
    }

    override fun emptyData() {
        ViewTooltip.on(fab)
                .color(getColorCompat(R.color.light_green))
                .corner(10).autoHide(true, 2000).padding(10, 10, 10, 10).align(ViewTooltip.ALIGN.CENTER)
                .position(ViewTooltip.Position.LEFT).text(R.string.info_group_create_item).setTextGravity(Gravity.CENTER)
                .show()
    }

    override fun displayFab() {
        fab!!.visibility = View.VISIBLE
    }

    companion object {

        private val EXTRA_GROUP_UID = "group_uid"
        private val REQUEST_PICK_CONTACTS = 1

        fun start(activity: Activity, groupUid: String) {
            val intent = Intent(activity, GroupDetailsActivity::class.java)
            intent.putExtra(EXTRA_GROUP_UID, groupUid)
            activity.startActivity(intent)
        }
    }
}
