package za.org.grassroot2.presenter.fragment

import java.util.ArrayList

import javax.inject.Inject

import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.SelectableItem
import za.org.grassroot2.view.FragmentView

/**
 * Created by qbasso on 21.09.2017.
 */

class ItemSelectionFragmentPresenter @Inject internal constructor(private val databaseService: DatabaseService) : BaseFragmentPresenter<ItemSelectionFragmentPresenter.ItemSelectionFragmentView>() {

    override fun onViewCreated() {
        val data = ArrayList<SelectableItem>()
        data.addAll(databaseService.loadObjects(Group::class.java))
        view.renderResults(data)
    }

    interface ItemSelectionFragmentView : FragmentView {
        fun renderResults(data: List<SelectableItem>)
    }
}
