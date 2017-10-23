package za.org.grassroot2.presenter;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.task.Meeting;
import za.org.grassroot2.model.task.Task;
import za.org.grassroot2.services.NetworkService;
import za.org.grassroot2.view.GrassrootView;


public class CreateActionPresenter extends BasePresenter<CreateActionPresenter.CreateActionView> {

    private NetworkService  networkService;
    private DatabaseService dbService;
    private Task            createdTask;

    @Inject
    public CreateActionPresenter(NetworkService networkService, DatabaseService dbService) {
        this.networkService = networkService;
        this.dbService = dbService;
        createdTask = new Meeting();
    }

    public void createMeeting() {
        view.showProgressBar();
        disposableOnDetach(networkService.createTask(createdTask).observeOn(AndroidSchedulers.mainThread()).subscribe(task -> {
            view.closeProgressBar();
            view.uploadSuccessfull();
        }, throwable -> {
            view.closeProgressBar();
            handleNetworkUploadError(throwable);
        }));
    }

    public void verifyGroupPermissions(String groupUid) {
        if (groupUid != null) {
            dbService.load(Group.class, groupUid).subscribe(group -> view.proceedWithRender(group), Throwable::printStackTrace);
        } else {
            view.proceedWithRender(null);
        }
    }

    public void setMeetingDate(Long date) {
        ((Meeting) createdTask).setMeetingDateTimeMillis(date);
        ((Meeting) createdTask).setCreatedDateTimeMillis(System.currentTimeMillis());
    }

    public void setMeetingLocation(String location) {
        ((Meeting) createdTask).setLocationDescription(location);
    }

    public void setSubject(String subject) {
        ((Meeting) createdTask).setSubject(subject);
    }

    public void setGroupUid(Group group) {
        ((Meeting) createdTask).setParentUid(group.getUid());
    }

    public Task getTask() {
        return createdTask;
    }

    public interface CreateActionView extends GrassrootView {
        void uploadSuccessfull();

        void closeScreen();

        void proceedWithRender(Group group);
    }

}
