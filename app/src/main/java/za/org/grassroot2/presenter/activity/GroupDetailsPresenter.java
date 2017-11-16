package za.org.grassroot2.presenter.activity;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import za.org.grassroot2.R;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.RequestMapper;
import za.org.grassroot2.model.contact.Contact;
import za.org.grassroot2.model.util.GroupPermissionChecker;
import za.org.grassroot2.services.NetworkService;
import za.org.grassroot2.view.GrassrootView;


public class GroupDetailsPresenter extends BasePresenter<GroupDetailsPresenter.GroupDetailsView>{

    private DatabaseService databaseService;
    private NetworkService  networkService;
    private String          groupUid;

    @Inject
    public GroupDetailsPresenter(DatabaseService service, NetworkService networkService) {
        databaseService = service;
        this.networkService = networkService;
    }

    public void init(String groupUid) {
        this.groupUid = groupUid;
    }

    public void loadData() {
        disposableOnDetach(databaseService.load(Group.class, groupUid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(group -> {
            if (getView() != null) {
                if (GroupPermissionChecker.hasCreatePermission(group)) {
                    getView().displayFab();
                }
                getView().render(group);
            }
        }, Throwable::printStackTrace));
        disposableOnDetach(networkService.getTasksForGroup(groupUid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(tasks -> {
            if (!tasks.isEmpty()) {
                databaseService.storeTasks(tasks);
                EventBus.getDefault().post(new TasksUpdatedEvent());
            } else {
                getView().emptyData();
            }
        }, this::handleNetworkConnectionError));

    }

    public void inviteContacts(List<Contact> contacts) {
        getView().showProgressBar();
        disposableOnDetach(networkService.inviteContactsToGroup(groupUid, RequestMapper.map(groupUid, contacts)).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(voidResponse -> {
            getView().closeProgressBar();
            if (voidResponse.isSuccessful()) {
            } else {
                getView().showErrorSnackbar(R.string.error_permission_denied);
            }
        }, this::handleNetworkUploadError));
    }

    public void inviteContact(String name, String phone) {
        Contact c = new Contact();
        c.setDisplayName(name);
        c.setPhoneNumber(phone);
        inviteContacts(Collections.singletonList(c));
    }

    public interface GroupDetailsView extends GrassrootView {
        void render(Group group);
        void emptyData();
        void displayFab();
    }

    public static class TasksUpdatedEvent {
    }
}
