package za.org.grassroot2.dagger.activity;

import dagger.Subcomponent;
import za.org.grassroot2.view.LoginActivity;
import za.org.grassroot2.view.activity.GrassrootActivity;
import za.org.grassroot2.view.activity.GroupDetailsActivity;
import za.org.grassroot2.view.activity.MainActivity;
import za.org.grassroot2.view.fragment.GroupTasksFragment;
import za.org.grassroot2.view.fragment.GroupsFragment;
import za.org.grassroot2.view.fragment.ItemSelectionFragment;
import za.org.grassroot2.view.fragment.SingleTextMultiButtonFragment;

/**
 * Created by luke on 2017/08/08.
 */
@PerActivity
@Subcomponent(modules = {ActivityModule.class})
public interface ActivityComponent {

    void inject(SingleTextMultiButtonFragment fragment);
    void inject(ItemSelectionFragment fragment);
    void inject(GrassrootActivity target);
    void inject(LoginActivity target);
    void inject(MainActivity target);
    void inject(GroupsFragment fragment);
    void inject(GroupDetailsActivity target);
    void inject(GroupTasksFragment fragment);
}