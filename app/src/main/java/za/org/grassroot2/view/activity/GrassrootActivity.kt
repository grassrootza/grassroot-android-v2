package za.org.grassroot2.view.activity

import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.annotation.LayoutRes
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.gcm.GoogleCloudMessaging
import dagger.Lazy
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.include_progress_bar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import za.org.grassroot.messaging.dto.MessageDTO
import za.org.grassroot2.GrassrootApplication
import za.org.grassroot2.R
import za.org.grassroot2.dagger.AppComponent
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.dagger.activity.ActivityModule
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.service.GCMPreferences
import za.org.grassroot2.services.OfflineReceiver
import za.org.grassroot2.services.SyncOfflineDataService
import za.org.grassroot2.services.account.AuthConstants
import za.org.grassroot2.services.rest.AddTokenInterceptor
import za.org.grassroot2.util.AlarmManagerHelper
import za.org.grassroot2.util.UserPreference
import za.org.grassroot2.util.ViewAnimation
import za.org.grassroot2.view.GrassrootView
import za.org.grassroot2.view.dialog.GenericErrorDialog
import za.org.grassroot2.view.dialog.GenericMessageDialog
import za.org.grassroot2.view.dialog.GenericSuccessDialog
import za.org.grassroot2.view.dialog.NoConnectionDialog
import java.io.IOException
import java.util.*
import javax.inject.Inject

abstract class GrassrootActivity : AppCompatActivity(), GrassrootView {

    @Inject lateinit var accountManagerProvider: Lazy<AccountManager>
    @Inject lateinit var userPreference: UserPreference
    @Inject lateinit var jsonMaper: ObjectMapper

    @get:LayoutRes protected abstract val layoutResourceId: Int
    protected abstract fun onInject(component: ActivityComponent)

    private var mRegistrationBroadcastReceiver: BroadcastReceiver? = null
    private var isReceiverRegistered: Boolean = false

    private var authResponse: AccountAuthenticatorResponse? = null
    private var authResultBundle: Bundle? = null
    protected var disposables = CompositeDisposable()

    private var component: ActivityComponent? = null

    override val activity: Activity
        get() = this

    val activityModule: ActivityModule
        get() = ActivityModule(this)

    val componenet: ActivityComponent
        get() {
            if (component == null) {
                component = appComponent.plus(activityModule)
            }
            return component!!
        }

    val appComponent: AppComponent
        get() = (application as GrassrootApplication).appComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.base_progress_container)
        setContentLayout(layoutResourceId)
        componenet.inject(this)
        onInject(componenet)
        authResponse = intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
        if (authResponse != null) {
            authResponse!!.onRequestContinued()
        }


        mRegistrationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val currentToken = sharedPreferences.getString(GCMPreferences.CURRENT_GCM_TOKEN, null)
                Timber.i("GCM token check finished. Current token: %s", currentToken)
                closeProgressBar()
            }
        }
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver!!,
                    IntentFilter(GCMPreferences.GCM_REGISTRATION_COMPLETE))
            isReceiverRegistered = true
        }
    }

    private fun setContentLayout(resId: Int) {
        val parent = findViewById<View>(R.id.main_layout) as RelativeLayout
        val v = LayoutInflater.from(this).inflate(resId, parent, false)
        parent.addView(v)
    }

    protected fun loggedIn(): Boolean {
        val accounts = accountManagerProvider.get().getAccountsByType(AuthConstants.ACCOUNT_TYPE)
        return accounts.size != 0 && !TextUtils.isEmpty(accountManagerProvider.get().getUserData(accounts[0], AuthConstants.USER_DATA_LOGGED_IN))
    }

    override fun showMessageDialog(text: String) {
        val dialog = GenericMessageDialog.newInstance(text)
        dialog.show(supportFragmentManager, DIALOG_TAG)
    }

    override fun handleNoConnection() {
        if (!userPreference.connectionInfoDisplayed()) {
            val dialog: DialogFragment
            if (loggedIn()) {
                dialog = NoConnectionDialog.newInstance(NoConnectionDialog.TYPE_AUTHORIZED)
            } else {
                dialog = NoConnectionDialog.newInstance(NoConnectionDialog.TYPE_NOT_AUTHORIZED)
            }
            dialog.show(supportFragmentManager, DIALOG_TAG)
            userPreference.setNoConnectionInfoDisplayed(true)
            AlarmManagerHelper.scheduleAlarmForBroadcastReceiver(this, OfflineReceiver::class.java)
        }
    }

    override fun handleNoConnectionUpload() {
        if (userPreference.connectionInfoDisplayed()) {
            showNoConnectionMessage()
        } else {
            handleNoConnection()
        }
    }

    override fun showSuccessSnackbar(successMsg: Int) {
        Snackbar.make(findViewById(android.R.id.content), successMsg, Toast.LENGTH_SHORT).show()
    }

    override fun showErrorDialog(errorMsg: Int) {
        val dialog = GenericErrorDialog.newInstance(errorMsg)
        dialog.show(supportFragmentManager, DIALOG_TAG)
    }

    override fun showErrorSnackbar(errorTextRes: Int) {
        Snackbar.make(findViewById(android.R.id.content), errorTextRes, Toast.LENGTH_SHORT).show()
    }

    override fun showSuccessDialog(textRes: Int, okayListener: View.OnClickListener) {
        val dialog = GenericSuccessDialog.newInstance(textRes, okayListener)
        dialog.show(supportFragmentManager, DIALOG_TAG)
    }

    override fun launchActivity(cls: Class<*>, args: Bundle) {
        val i = Intent(this, cls)
        i.putExtras(args)
        startActivity(i)
    }

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     *
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    fun setAccountAuthenticatorResult(result: Bundle) {
        authResultBundle = result
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    override fun finish() {
        if (authResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (authResultBundle != null) {
                authResponse!!.onResult(authResultBundle)
            } else {
                authResponse!!.onError(AccountManager.ERROR_CODE_CANCELED,
                        "canceled")
            }
            authResponse = null
        }
        super.finish()
    }

    override fun closeKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun showProgressBar() {
        Timber.d("showing progress bar 1 in activity: %s", activity.toString())
        progress?.let { ViewAnimation.fadeIn(progress!!) }
    }

    override fun closeProgressBar() {
        Timber.d("showing progress bar 2 inside activity")
        progress?.let { ViewAnimation.fadeOut(progress!!) }
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver!!)
        isReceiverRegistered = false
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissDialogs()
        if (!disposables.isDisposed) {
            disposables.clear()
        }
    }

    private fun dismissDialogs() {
        val fragmentByTag = supportFragmentManager.findFragmentByTag(DIALOG_TAG)
        if (fragmentByTag != null) {
            (fragmentByTag as DialogFragment).dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)

        //        if (loggedIn()) {
        //
        //            if (checkPlayServices()) {
        //                Timber.e("Showing progress bar 3 in activity: %s", getActivity().toString());
        //                // start registration service in order to check token and register if not already registered
        //                showProgressBar();
        //                registerReceiver();
        //                Intent intent = new Intent(this, GCMRegistrationService.class);
        //                startService(intent);
        //            }
        //        }
    }

    @Subscribe(sticky = true)
    fun tokenRefreshEvent(e: AddTokenInterceptor.TokenRefreshEvent) {
        EventBus.getDefault().removeStickyEvent(e)
        val accountManager = accountManagerProvider!!.get()
        val accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE)
        accountManager.getAuthToken(accounts[0], AuthConstants.AUTH_TOKENTYPE, null, this, { future ->
            try {
                if (future.result.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                    accountManager.setAuthToken(accounts[0], AuthConstants.AUTH_TOKENTYPE, future.result.getString(AccountManager.KEY_AUTHTOKEN))
                    accountManager.setUserData(accounts[0], AuthConstants.USER_DATA_CURRENT_TOKEN, future.result.getString(AccountManager.KEY_AUTHTOKEN))
                }
            } catch (e1: OperationCanceledException) {
                e1.printStackTrace()
            } catch (e1: IOException) {
                e1.printStackTrace()
            } catch (e1: AuthenticatorException) {
                e1.printStackTrace()
            }
        }, null)
    }

    @Subscribe
    fun notifyItemOutOfSync(e: SyncOfflineDataService.ObjectOutOfSyncEvent) {
        if (e.syncable is Meeting) {
            Snackbar.make(findViewById(android.R.id.content), e.msg!!, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun showNoConnectionMessage() {
        Snackbar.make(findViewById(android.R.id.content), R.string.snackbar_offline, Snackbar.LENGTH_SHORT).show()
    }

    override fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    private fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show()
            } else {
                Timber.i("This device is not supported.")
                finish()
            }
            return false
        }
        return true
    }

    override fun sendCGMMessage(messageDTO: MessageDTO) {
        try {
            val gcm = GoogleCloudMessaging.getInstance(this)
            val data = Bundle()
            val msgJson = jsonMaper!!.writeValueAsString(messageDTO)
            data.putString("body", msgJson)

            val id = UUID.randomUUID().toString()
            val senderId = getString(R.string.gcm_sender_id)
            gcm.send("$senderId@gcm.googleapis.com", id, data)
        } catch (ex: Exception) {
            Timber.e(ex)
        }

    }

    companion object {
        const val DIALOG_TAG = "dialog"
        const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    }
}
