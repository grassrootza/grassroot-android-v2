package za.org.grassroot2.presenter.fragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.SelectableItem;
import za.org.grassroot2.view.FragmentView;

/**
 * Created by qbasso on 21.09.2017.
 */

public class ItemSelectionFragmentPresenter extends BaseFragmentPresenter<ItemSelectionFragmentPresenter.ItemSelectionFragmentView> {

    private final DatabaseService databaseService;

    @Inject ItemSelectionFragmentPresenter(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void onViewCreated() {
        List<SelectableItem> data = new ArrayList<>();
        data.addAll(databaseService.loadObjectsForSelection(Group.class));
        view.renderResults(data);
    }

    public interface ItemSelectionFragmentView extends FragmentView {
        void renderResults(List<SelectableItem> data);
    }
}
