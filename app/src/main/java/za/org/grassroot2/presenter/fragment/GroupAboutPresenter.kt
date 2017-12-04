package za.org.grassroot2.presenter.fragment

import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Group
import za.org.grassroot2.view.FragmentView
import javax.inject.Inject

/**
 * Created by luke on 2017/12/04.
 */
class GroupAboutPresenter @Inject
constructor(private val databaseService: DatabaseService): BaseFragmentPresenter<GroupAboutPresenter.GroupAboutView>() {

    private var groupUid: String? = null

    override fun onViewCreated() { }

    fun init(groupUid: String) {
        this.groupUid = groupUid
    }

    fun loadGroup() {
        disposableOnDetach(databaseService.load(Group::class.java, groupUid!!).subscribeOn(io()).observeOn(main()).subscribe({ group ->
            view.render(group)
        }))
    }

    interface GroupAboutView : FragmentView {
        fun render(group: Group)
    }

}