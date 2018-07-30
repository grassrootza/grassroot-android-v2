package za.org.grassroot2.presenter.activity

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import za.org.grassroot2.model.contact.Contact
import za.org.grassroot2.util.ContactHelper
import za.org.grassroot2.view.GrassrootView
import java.util.*
import javax.inject.Inject


class PickContactPresenter @Inject
constructor(private val contactHelper: ContactHelper) : BasePresenter<PickContactPresenter.PickContactView>() {

    fun loadContacts() {
        timber.log.Timber.d("Showing solitary progress bar in PickContactPresenter")
        view.showProgressBar()
        disposableOnDetach(contactHelper.allContactsSimple.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).flatMap { contacts ->
            view.closeProgressBar()
            if (contacts.isEmpty()) {
                view.emptyData()
            } else {
                view.render(contacts)
            }
            contactHelper.allContacts.subscribeOn(Schedulers.io())
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ contacts ->
            if (!contacts.isEmpty()) {
                view.render(contacts)
            }
        }, { Timber.e(it) }))
    }

    fun loadContactsForIds(selectedItems: List<Long>): List<Contact> {
        val result = ArrayList<Contact>()
        for (l in selectedItems) {
            result.add(contactHelper.getContact(l))
        }
        return result
    }

    interface PickContactView : GrassrootView {
        fun render(group: List<Contact>)
        fun emptyData()
    }

}
