package za.org.grassroot2.presenter.fragment;


import javax.inject.Inject;

import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.view.FragmentView;

public class ItemCalledPresenter extends BaseFragmentPresenter<ItemCalledPresenter.MeetingCalledView> {

    private final DatabaseService    databaseService;

    @Inject
    public ItemCalledPresenter(DatabaseService dbService) {
        databaseService = dbService;
    }

    public void loadGroupData(String groupUid, GrassrootEntityType type) {
        disposableOnDetach(databaseService.load(Group.class, groupUid).subscribe(group -> view.showDescription(group.getMemberCount(), type), Throwable::printStackTrace));
    }

    @Override
    public void onViewCreated() {
    }

    public interface MeetingCalledView extends FragmentView{
        void showDescription(Integer memberCount, GrassrootEntityType type);
    }
}
