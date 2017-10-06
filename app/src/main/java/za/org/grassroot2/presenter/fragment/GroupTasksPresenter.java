package za.org.grassroot2.presenter.fragment;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.task.Task;
import za.org.grassroot2.services.NetworkService;
import za.org.grassroot2.services.Status;
import za.org.grassroot2.view.FragmentView;

public class GroupTasksPresenter extends BaseFragmentPresenter<GroupTasksPresenter.AllFragmentView> {

    private NetworkService api;

    @Inject
    public GroupTasksPresenter() {
    }

    @Override
    public void onViewCreated() {
    }

    public void loadTasks(String groupUid, GrassrootEntityType type) {
        api.getTasks(groupUid, type).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(tasks -> {
            if (!tasks.data.isEmpty()) {
                Collections.sort(tasks.data, (o1, o2) -> o1.getDeadlineMillis() < o2.getDeadlineMillis() ? 1 : (o1.getDeadlineMillis() > o2.getDeadlineMillis() ? -1 : 0));
                view.render(tasks.data);
            } else if (tasks.status == Status.SUCCESS) {
                EventBus.getDefault().post(new GroupTasksEmptyEvent());
                view.empty();
            }
        }, Throwable::printStackTrace);
    }

    @Inject
    public void setApi(NetworkService api) {
        this.api = api;
    }

    public interface AllFragmentView extends FragmentView {
        void render(List<Task> tasks);

        void empty();
    }

    public static class GroupTasksEmptyEvent {
    }
}
