package za.org.grassroot2.presenter.fragment;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.services.UserDetailsService;
import za.org.grassroot2.services.account.SyncAdapter;
import za.org.grassroot2.view.FragmentView;

public class GroupFragmentPresenter extends BaseFragmentPresenter<GroupFragmentPresenter.GroupFragmentView> {

    private final DatabaseService    databaseService;
    private final UserDetailsService userDetailsService;
    private boolean firstSyncNotCompleted;

    @Inject
    public GroupFragmentPresenter(DatabaseService dbService, UserDetailsService userDetailsService) {
        databaseService = dbService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void attach(GroupFragmentView view) {
        super.attach(view);
    }

    @Override
    public void detach(GroupFragmentView view) {
        super.detach(view);
    }

    @Override
    public void onViewCreated() {
        firstSyncNotCompleted = !userDetailsService.isSyncFailed() && !userDetailsService.isSyncCompleted();
        if (userDetailsService.isSyncFailed()) {
            view.closeProgressBar();
            view.renderEmptyFailedSync();
        } else {
            if (firstSyncNotCompleted) {
                view.showProgressBar();
            } else {
                view.closeProgressBar();
            }
            loadGroups();
        }
    }

    private void loadGroups() {
        List<Group> groups = databaseService.loadObjects(Group.class);
        if (groups.isEmpty() && !firstSyncNotCompleted) {
            view.closeProgressBar();
            view.renderEmpty();
        } else {
            view.render(groups);
            disposableOnDetach(view.itemClick().subscribe(s -> view.openDetails(s), Throwable::printStackTrace));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void syncComplete(SyncAdapter.SyncCompletedEvent e) {
        EventBus.getDefault().removeStickyEvent(SyncAdapter.SyncCompletedEvent.class);
        onViewCreated();
    }

    public interface GroupFragmentView extends FragmentView {
        void render(List<Group> groups);

        void renderEmpty();

        Observable<String> itemClick();

        void renderEmptyFailedSync();

        void openDetails(String groupUid);
    }
}
