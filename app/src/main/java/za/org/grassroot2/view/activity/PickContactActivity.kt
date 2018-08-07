package za.org.grassroot2.view.activity


import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
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
import za.org.grassroot2.view.dialog.GenericSuccessDialog
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

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView.maxWidth = Integer.MAX_VALUE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                adapter!!.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter!!.filter.filter(newText)
                return false
            }
        })

        val actionButton = findViewById<FloatingActionButton>(R.id.done)
        actionButton.setOnClickListener { v ->
            val selectedItems = adapter!!.getSelectedItems()
            if (!selectedItems.isEmpty()) {
                val contacts = presenter.loadContactsForIds(selectedItems)
                val data = Intent()
                data.putExtra(EXTRA_CONTACTS, contacts as ArrayList<Contact>)
                setResult(Activity.RESULT_OK, data)

                val message = getString(R.string.members_added, contacts.size)

                val dialogFragment = GenericSuccessDialog.newInstance(R.string.members_added,
                        message, View.OnClickListener { finish() })

                dialogFragment.show(supportFragmentManager, "dialog")
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /*switch (item.getItemId()) {
            default:
                List<Long> selectedItems = adapter.getSelectedItems();
                if (!selectedItems.isEmpty()) {
                    List<Contact> contacts = presenter.loadContactsForIds(selectedItems);
                    Intent data = new Intent();
                    data.putExtra(EXTRA_CONTACTS, (ArrayList<Contact>)contacts);
                    setResult(RESULT_OK, data);
                    finish();
                }
                return true;
        }*/
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
