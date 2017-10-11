package za.org.grassroot2.presenter;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.services.NetworkService;
import za.org.grassroot2.view.GrassrootView;


public class GroupDetailsPresenter extends BasePresenter<GroupDetailsPresenter.GroupDetailsView>{

    private DatabaseService databaseService;
    private NetworkService  networkService;

    @Inject
    public GroupDetailsPresenter(DatabaseService service, NetworkService networkService) {
        databaseService = service;
        this.networkService = networkService;
    }

    public void loadData(String groupUid) {
        disposableOnDetach(databaseService.load(Group.class, groupUid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(group -> {
            if (view != null) {
                view.render(group);
            }
        }, Throwable::printStackTrace));
        disposableOnDetach(networkService.getTasksForGroup(groupUid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(tasks -> {
            if (!tasks.isEmpty()) {
                databaseService.storeTasks(tasks);
                EventBus.getDefault().post(new TasksUpdatedEvent());
            } else {
                view.emptyData();
            }
        }, Throwable::printStackTrace));

    }

    public interface GroupDetailsView extends GrassrootView {
        void render(Group group);
        void emptyData();
    }

    public static class TasksUpdatedEvent {
    }
}
