package za.org.grassroot2.presenter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import za.org.grassroot2.model.contact.Contact;
import za.org.grassroot2.util.ContactHelper;
import za.org.grassroot2.view.GrassrootView;


public class PickContactPresenter extends BasePresenter<PickContactPresenter.PickContactView>{

    private ContactHelper contactHelper;

    @Inject
    public PickContactPresenter(ContactHelper helper) {
        contactHelper = helper;
    }

    public void loadContacts() {
        view.showProgressBar();
        disposableOnDetach(contactHelper.getAllContactsSimple().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).flatMap(contacts -> {
            view.closeProgressBar();
            if (contacts.isEmpty()) {
                view.emptyData();
            } else {
                view.render(contacts);
            }
            return contactHelper.getAllContacts().subscribeOn(Schedulers.io());
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(contacts -> {
            if (!contacts.isEmpty()) {
                view.render(contacts);
            }
        }, Throwable::printStackTrace));
    }

    public List<Contact> loadContactsForIds(List<Long> selectedItems) {
        List<Contact> result = new ArrayList<>();
        for (Long l : selectedItems) {
            result.add(contactHelper.getContact(l));
        }
        return result;
    }

    public interface PickContactView extends GrassrootView {
        void render(List<Contact> group);
        void emptyData();
    }

}
