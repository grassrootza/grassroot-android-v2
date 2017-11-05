package za.org.grassroot2.dagger.activity;

import dagger.Subcomponent;
import za.org.grassroot2.view.activity.CreateActionActivity;
import za.org.grassroot2.view.activity.GrassrootActivity;
import za.org.grassroot2.view.activity.GroupDetailsActivity;
import za.org.grassroot2.view.activity.LoginActivity;
import za.org.grassroot2.view.activity.LoginActivity2;
import za.org.grassroot2.view.activity.PickContactActivity;
import za.org.grassroot2.view.activity.RegisterActivity;
import za.org.grassroot2.view.activity.WelcomeActivity;
import za.org.grassroot2.view.fragment.GrassrootFragment;
import za.org.grassroot2.view.fragment.GroupSelectionFragment;
import za.org.grassroot2.view.fragment.GroupTasksFragment;
import za.org.grassroot2.view.fragment.GroupsFragment;
import za.org.grassroot2.view.fragment.ItemCreatedFragment;
import za.org.grassroot2.view.fragment.ItemSelectionFragment;
import za.org.grassroot2.view.fragment.MeetingDateFragment;
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

    void inject(LoginActivity2 target);
    void inject(GroupsFragment fragment);
    void inject(GroupDetailsActivity target);
    void inject(GroupTasksFragment fragment);
    void inject(GrassrootFragment grassrootFragment);
    void inject(PickContactActivity activity);
    void inject(GroupSelectionFragment fragment);
    void inject(MeetingDateFragment fragment);
    void inject(CreateActionActivity activity);
    void inject(ItemCreatedFragment fragment);

    void inject(WelcomeActivity target);

    void inject(RegisterActivity target);
}