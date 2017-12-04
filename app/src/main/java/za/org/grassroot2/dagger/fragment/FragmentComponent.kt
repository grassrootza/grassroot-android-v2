package za.org.grassroot2.dagger.fragment

import dagger.Subcomponent
import za.org.grassroot2.view.fragment.AroundMeFragment
import za.org.grassroot2.view.fragment.GroupAboutFragment
import za.org.grassroot2.view.fragment.HomeFragment
import za.org.grassroot2.view.fragment.MeFragment

/**
 * Created by luke on 2017/08/08.
 */
@PerFragment
@Subcomponent(modules = arrayOf(FragmentModule::class))
interface FragmentComponent {
    fun inject(aroundMeFragment: AroundMeFragment)
    fun inject(fragment: HomeFragment)
    fun inject(fragment: MeFragment)
    fun inject(fragment: GroupAboutFragment)
}
