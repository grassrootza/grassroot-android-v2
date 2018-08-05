package za.org.grassroot2.view.fragment

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import butterknife.OnClick
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_group_selection.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.SelectableItem
import za.org.grassroot2.presenter.fragment.GroupSelectionPresenter
import za.org.grassroot2.view.adapter.GroupSelectionAdapter
import java.util.*
import javax.inject.Inject

/**
 * Created by qbasso on 18.10.2017.
 */

class GroupSelectionFragment : GrassrootFragment(), GroupSelectionPresenter.GroupSelectionView {
    override val layoutResourceId: Int
        get() = R.layout.fragment_group_selection

    private val itemSelectedSubject = PublishSubject.create<Group>()

    @Inject internal lateinit var presenter: GroupSelectionPresenter
    private var adapter: GroupSelectionAdapter? = null
    private var listener: BackNavigationListener? = null

    fun itemSelection(): Observable<Group> = itemSelectedSubject
    override fun searchChanged(): Observable<String> = RxTextView.textChanges(searchInput!!).map { it.toString() }

    override fun groupClick(): Observable<Group> = adapter!!.viewClickObservable
    override fun groupSelected(g: Group) = itemSelectedSubject.onNext(g)

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = activity as BackNavigationListener?
    }

    @OnClick(R.id.backNav)
    internal fun back() {
        listener!!.backPressed()
    }

    @OnClick(R.id.close)
    internal fun close() {
        activity!!.finish()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        groupRecyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = GroupSelectionAdapter(ArrayList())
        groupRecyclerView.adapter = adapter
        presenter.attach(this)
        presenter.onViewCreated()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach(this)
    }

    override fun onInject(activityComponent: ActivityComponent) {
        activityComponent.inject(this)
    }


    override fun renderResults(data: List<SelectableItem>) {
        adapter!!.updateData(data)
    }

}
