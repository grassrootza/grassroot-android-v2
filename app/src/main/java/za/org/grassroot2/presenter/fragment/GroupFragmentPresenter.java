package za.org.grassroot2.presenter.fragment;


import java.util.List;

import javax.inject.Inject;

import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.services.NetworkService;
import za.org.grassroot2.view.FragmentView;

public class GroupFragmentPresenter extends BaseFragmentPresenter<GroupFragmentPresenter.GroupFragmentView> {

    private final DatabaseService databaseService;
    private final NetworkService networkService;

    @Inject
    public GroupFragmentPresenter(DatabaseService dbService, NetworkService networkService) {
        databaseService = dbService;
        this.networkService = networkService;
    }

    @Override
    public void onViewCreated() {
        List<Group> groups = databaseService.loadObjectsForSelection(Group.class);
        if (groups.isEmpty()) {
            view.renderEmpty();
        } else {
            view.render(groups);
        }
    }

    public interface GroupFragmentView extends FragmentView {
        void render(List<Group> groups);
        void renderEmpty();
    }
}
