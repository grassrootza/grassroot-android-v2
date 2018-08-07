package za.org.grassroot2.view.fragment

import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.Unbinder
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import za.org.grassroot2.GrassrootApplication
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.dagger.fragment.FragmentComponent
import za.org.grassroot2.dagger.fragment.FragmentModule
import za.org.grassroot2.services.OfflineReceiver
import za.org.grassroot2.services.account.AuthConstants
import za.org.grassroot2.util.AlarmManagerHelper
import za.org.grassroot2.util.UserPreference
import za.org.grassroot2.util.ViewAnimation
import za.org.grassroot2.view.FragmentView
import za.org.grassroot2.view.activity.GrassrootActivity
import za.org.grassroot2.view.dialog.GenericErrorDialog
import za.org.grassroot2.view.dialog.NoConnectionDialog
import javax.inject.Inject

/**
 * Created by luke on 2017/08/10.
 */

abstract class GrassrootFragment : Fragment(), FragmentView {

    protected var unbinder: Unbinder? = null
    protected var lifecyclePublisher = PublishSubject.create<Int>()
    protected var disposables = CompositeDisposable()

    internal var progress: View? = null

    @Inject internal lateinit var userPreference: UserPreference
    @Inject lateinit var accountManagerProvider: Lazy<AccountManager>

    private var component: FragmentComponent? = null

    @get:LayoutRes

    abstract val layoutResourceId: Int

    protected abstract fun onInject(activityComponent: ActivityComponent)

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val application = activity!!.application as GrassrootApplication
        val activityComponent = application.appComponent.plus((activity as GrassrootActivity).activityModule)
        activityComponent.inject(this)
        onInject(activityComponent)
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        Timber.i("inside GrassrootFragment onDestroyView")
        disposables.clear()
        component = null
        if (this.unbinder != null) {
            unbinder!!.unbind()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(layoutResourceId, container, false)
        unbinder = ButterKnife.bind(this, v)
        progress = activity!!.findViewById(R.id.progress)
        return v
    }

    override fun viewCreated(): Observable<Int> = lifecyclePublisher
                .filter { integer -> integer == ACTION_FRAGMENT_VIEW_CREATED }

    override fun showNoConnectionMessage() {
        (activity as GrassrootActivity).showNoConnectionMessage()
    }


    override fun showErrorDialog(errorMsgResId: Int) {
        val dialog = GenericErrorDialog.newInstance(errorMsgResId)
        dialog.show(childFragmentManager, DIALOG_TAG)
    }


    override fun handleNoConnection() {
        if (!userPreference.connectionInfoDisplayed()) {
            val dialog: DialogFragment
            if (loggedIn()) {
                dialog = NoConnectionDialog.newInstance(NoConnectionDialog.TYPE_AUTHORIZED)
            } else {
                dialog = NoConnectionDialog.newInstance(NoConnectionDialog.TYPE_NOT_AUTHORIZED)
            }
            dialog.show(childFragmentManager, DIALOG_TAG)
            userPreference.setNoConnectionInfoDisplayed(true)
            AlarmManagerHelper.scheduleAlarmForBroadcastReceiver(activity!!, OfflineReceiver::class.java)
        }
    }

    override fun handleNoConnectionUpload() {
        if (userPreference.connectionInfoDisplayed()) {
            showNoConnectionMessage()
        } else {
            handleNoConnection()
        }
    }

    protected fun loggedIn(): Boolean {
        val accounts = accountManagerProvider.get().getAccountsByType(AuthConstants.ACCOUNT_TYPE)
        return accounts.size != 0 && !TextUtils.isEmpty(accountManagerProvider.get().getUserData(accounts[0], AuthConstants.USER_DATA_LOGGED_IN))
    }

    override fun showProgressBar() {
        progress?.let { ViewAnimation.fadeOut(progress!!) }
    }

    override fun closeProgressBar() {
        progress?.let { ViewAnimation.fadeOut(progress!!) }
    }

    protected fun get(): FragmentComponent {
        if (component == null) {
            component = (activity as GrassrootActivity).componenet.plus(FragmentModule())
        }
        return component as FragmentComponent
    }

    companion object {

        const val ACTION_FRAGMENT_CREATED = 2
        const val ACTION_FRAGMENT_VIEW_CREATED = 3
        const val DIALOG_TAG = "dialog"

    }
}
