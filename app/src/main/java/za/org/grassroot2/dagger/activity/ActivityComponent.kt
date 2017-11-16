package za.org.grassroot2.dagger.activity

import dagger.Subcomponent
import za.org.grassroot2.dagger.fragment.FragmentComponent
import za.org.grassroot2.dagger.fragment.FragmentModule
import za.org.grassroot2.view.activity.*
import za.org.grassroot2.view.fragment.*

/**
 * Created by luke on 2017/08/08.
 */
@PerActivity
@Subcomponent(modules = arrayOf(ActivityModule::class))
interface ActivityComponent {

    fun inject(fragment: ItemSelectionFragment)
    fun inject(target: GrassrootActivity)
    fun inject(target: DashboardActivity)
    fun inject(target: LoginActivity)
    fun inject(fragment: GroupsFragment)
    fun inject(target: GroupDetailsActivity)
    fun inject(fragment: GroupTasksFragment)
    fun inject(grassrootFragment: GrassrootFragment)
    fun inject(activity: PickContactActivity)
    fun inject(fragment: GroupSelectionFragment)
    fun inject(fragment: MeetingDateFragment)
    fun inject(activity: CreateActionActivity)
    fun inject(fragment: ItemCreatedFragment)
    fun inject(activity: WelcomeActivity)
    fun inject(activity: RegisterActivity)
    fun inject(activity: ForgotPasswordActivity)
    fun inject(activity: MeetingDetailsActivity)
    fun inject(activity: CreatePostActivity)
    fun plus(module: FragmentModule) : FragmentComponent

}