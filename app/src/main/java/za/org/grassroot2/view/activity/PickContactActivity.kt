package za.org.grassroot2.view.activity


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_pick_contact.*
import kotlinx.android.synthetic.main.include_empty_info.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.contact.Contact
import za.org.grassroot2.presenter.activity.PickContactPresenter
import za.org.grassroot2.view.adapter.ContactsAdapter
import java.util.*
import javax.inject.Inject

class PickContactActivity : GrassrootActivity(), PickContactPresenter.PickContactView {

    @Inject internal lateinit var presenter: PickContactPresenter
    private var adapter: ContactsAdapter? = null

    override val layoutResourceId: Int
        get() = R.layout.activity_pick_contact

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.attach(this)
        contactList!!.layoutManager = LinearLayoutManager(this)
        adapter = ContactsAdapter(this, ArrayList())
        contactList!!.adapter = adapter
        presenter.loadContacts()
        setSupportActionBar(toolbar)
    }

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    override fun render(contacts: List<Contact>) {
        adapter!!.update(contacts)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.pick_contact, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val selectedItems = adapter!!.getSelectedItems()
        if (!selectedItems.isEmpty()) {
            val contacts = presenter.loadContactsForIds(selectedItems)
            val data = Intent()
            data.putExtra(EXTRA_CONTACTS, contacts as ArrayList<Contact>)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
        return true
    }

    override fun emptyData() {
        emptyInfo.visibility = View.VISIBLE
        emptyInfo.setText(R.string.no_contacts)
    }

    companion object {

        const val EXTRA_CONTACTS = "contacts"

        fun startForResult(activity: Activity, requestCode: Int) {
            activity.startActivityForResult(Intent(activity, PickContactActivity::class.java), requestCode)
        }
    }
}
