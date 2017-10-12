package za.org.grassroot2.presenter.fragment;

import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.task.Task;
import za.org.grassroot2.presenter.GroupDetailsPresenter;
import za.org.grassroot2.view.FragmentView;

public class GroupTasksPresenter extends BaseFragmentPresenter<GroupTasksPresenter.AllFragmentView> {

    private DatabaseService databaseService;
    private String groupUid;
    private GrassrootEntityType type;

    @Inject
    public GroupTasksPresenter() {
    }

    @Override
    public void onViewCreated() {
    }

    public void loadTasks() {
        disposableOnDetach(databaseService.loadTasksForGroup(groupUid, type).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(tasks -> {
            if (!tasks.isEmpty()) {
                Collections.sort(tasks, (o1, o2) -> o1.getDeadlineMillis() < o2.getDeadlineMillis() ? 1 : (o1.getDeadlineMillis() > o2.getDeadlineMillis() ? -1 : 0));
                view.render(tasks);
            } else {
                view.empty();
            }
        }, Throwable::printStackTrace));
    }

    public void init(String groupUid, GrassrootEntityType type) {
        this.groupUid = groupUid;
        this.type = type;
    }

    @Inject
    public void setDatabaseService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public interface AllFragmentView extends FragmentView {
        void render(List<Task> tasks);

        void empty();
    }

    @Subscribe
    public void refreshData(GroupDetailsPresenter.TasksUpdatedEvent e) {
        loadTasks();
    }

}
