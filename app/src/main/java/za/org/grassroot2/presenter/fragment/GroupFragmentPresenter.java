package za.org.grassroot2.presenter.fragment;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.services.UserDetailsService;
import za.org.grassroot2.services.account.SyncAdapter;
import za.org.grassroot2.view.FragmentView;

public class GroupFragmentPresenter extends BaseFragmentPresenter<GroupFragmentPresenter.GroupFragmentView> {

    private final DatabaseService    databaseService;
    private final UserDetailsService userDetailsService;

    @Inject
    public GroupFragmentPresenter(DatabaseService dbService, UserDetailsService userDetailsService) {
        databaseService = dbService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void attach(GroupFragmentView view) {
        super.attach(view);
        EventBus.getDefault().register(this);
    }

    @Override
    public void detach(GroupFragmentView view) {
        super.detach(view);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onViewCreated() {
        if (!userDetailsService.isSyncCompleted()) {
            view.renderEmptyFailedSync();
        } else {
            loadGroups();
        }
    }

    private void loadGroups() {
        List<Group> groups = databaseService.loadObjectsForSelection(Group.class);
        if (groups.isEmpty()) {
            view.renderEmpty();
        } else {
            view.render(groups);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void syncComplete(SyncAdapter.SyncCompletedEvent e) {
        onViewCreated();
    }

    public interface GroupFragmentView extends FragmentView {
        void render(List<Group> groups);

        void renderEmpty();

        void renderEmptyFailedSync();
    }
}
