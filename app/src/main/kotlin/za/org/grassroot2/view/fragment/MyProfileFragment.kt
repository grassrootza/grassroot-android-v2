package za.org.grassroot2.view.fragment

import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.view.MyProfileView

class MyProfileFragment : GrassrootFragment(), MyProfileView {

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_me
    }
}