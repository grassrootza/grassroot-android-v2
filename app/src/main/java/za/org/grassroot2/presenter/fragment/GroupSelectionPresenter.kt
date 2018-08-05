package za.org.grassroot2.presenter.fragment

import java.util.ArrayList
import java.util.concurrent.TimeUnit

import javax.inject.Inject

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.SelectableItem
import za.org.grassroot2.services.UserDetailsService
import za.org.grassroot2.view.FragmentView

/**
 * Created by qbasso on 21.09.2017.
 */

class GroupSelectionPresenter @Inject
internal constructor(private val databaseService: DatabaseService) : BaseFragmentPresenter<GroupSelectionPresenter.GroupSelectionView>() {

    override fun onViewCreated() {
        val data = ArrayList<SelectableItem>()
        data.addAll(databaseService.loadGroupsSorted())
        view.renderResults(data)
        disposableOnDetach(view.searchChanged().debounce(500, TimeUnit.MILLISECONDS).subscribeOn(io())
                .flatMap { s -> Observable.just(databaseService.loadObjectsByName(Group::class.java, s)) }
                .observeOn(main())
                .subscribe({ groups -> view.renderResults(groups) }, { it.printStackTrace() }))
        disposableOnDetach(view.groupClick().subscribe({ s -> view.groupSelected(s) }, { it.printStackTrace() }))
    }

    interface GroupSelectionView : FragmentView {
        fun renderResults(data: List<SelectableItem>)
        fun searchChanged(): Observable<String>
        fun groupClick(): Observable<Group>
        fun groupSelected(g: Group)
    }
}
