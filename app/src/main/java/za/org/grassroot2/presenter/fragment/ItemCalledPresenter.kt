package za.org.grassroot2.presenter.fragment


import timber.log.Timber
import javax.inject.Inject

import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.view.FragmentView

class ItemCalledPresenter @Inject
constructor(private val databaseService: DatabaseService) : BaseFragmentPresenter<ItemCalledPresenter.MeetingCalledView>() {

    fun loadGroupData(groupUid: String, type: GrassrootEntityType) {
        disposableOnDetach(databaseService.load(Group::class.java, groupUid)
                .subscribe({ group -> view.showDescription(group.memberCount, type) }, { Timber.e(it) }))
    }

    override fun onViewCreated() {}

    interface MeetingCalledView : FragmentView {
        fun showDescription(memberCount: Int?, type: GrassrootEntityType)
    }
}
