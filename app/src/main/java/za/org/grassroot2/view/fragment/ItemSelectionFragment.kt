package za.org.grassroot2.view.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_selectable_item_list.*
import org.greenrobot.eventbus.EventBus
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.model.SelectableItem
import za.org.grassroot2.presenter.fragment.ItemSelectionFragmentPresenter
import za.org.grassroot2.view.adapter.ItemSelectionAdapter
import javax.inject.Inject

class ItemSelectionFragment : GrassrootFragment(), ItemSelectionFragmentPresenter.ItemSelectionFragmentView {
    override val layoutResourceId: Int
        get() = R.layout.fragment_selectable_item_list

    private var recyclerViewAdapter: ItemSelectionAdapter? = null

    @Inject lateinit internal var presenter: ItemSelectionFragmentPresenter

    override fun onInject(activityComponent: ActivityComponent) = activityComponent.inject(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        if (arguments != null && arguments!!.containsKey(HEADER_STRING)) {
            listHeader.visibility = View.VISIBLE
            listHeader.setText(arguments!!.getInt(HEADER_STRING))
        } else {
            listHeader.visibility = View.GONE
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.attach(this)
        presenter.onViewCreated()
    }

    override fun onDetach() {
        super.onDetach()
        presenter.detach(this)
    }

    override fun renderResults(data: List<SelectableItem>) {
        recyclerViewAdapter = ItemSelectionAdapter(data)
        listRecyclerView.adapter = recyclerViewAdapter
        listRecyclerView.layoutManager = LinearLayoutManager(activity)
        disposables.add(recyclerViewAdapter!!.viewClickObservable.subscribe { s -> EventBus.getDefault().post(SelectionEvent(s)) })
    }

    class SelectionEvent internal constructor(val s: String)

    companion object {

        private val HEADER_STRING = "HEADER"

        fun newInstance(headerTextRes: Int): ItemSelectionFragment {
            val fragment = ItemSelectionFragment()
            val args = Bundle()
            args.putInt(HEADER_STRING, headerTextRes)
            fragment.arguments = args
            return fragment
        }
    }
}
