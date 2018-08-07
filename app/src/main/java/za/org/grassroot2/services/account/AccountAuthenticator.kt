package za.org.grassroot2.services.account

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.NetworkErrorException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils

import javax.inject.Inject

import timber.log.Timber
import za.org.grassroot2.GrassrootApplication
import za.org.grassroot2.services.UserDetailsService
import za.org.grassroot2.services.rest.GrassrootAuthApi
import za.org.grassroot2.view.activity.LoginActivity

class AccountAuthenticator(private val context: Context) : AbstractAccountAuthenticator(context) {

    @Inject internal lateinit var authApi: GrassrootAuthApi
    @Inject internal lateinit var userService: UserDetailsService

    init {
        (context.applicationContext as GrassrootApplication).appComponent.inject(this)
        Timber.e("created the account authenticator")
    }

    override fun editProperties(accountAuthenticatorResponse: AccountAuthenticatorResponse, s: String): Bundle? {
        return null
    }


    @Throws(NetworkErrorException::class)
    override fun addAccount(accountAuthenticatorResponse: AccountAuthenticatorResponse, accountType: String,
                            authTokenType: String, features: Array<String>, options: Bundle): Bundle {
        Timber.e("adding an account! inside authenticator, of type: %s", accountType)
        val intent = Intent(context, LoginActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse)
        intent.putExtra(EXTRA_NEW_ACCOUNT, true)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    @Throws(NetworkErrorException::class)
    override fun confirmCredentials(accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, bundle: Bundle): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun getAuthToken(accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, authTokenType: String, bundle: Bundle): Bundle {
        val am = AccountManager.get(context)
        val authToken = am.peekAuthToken(account, authTokenType)
        val result = Bundle()
        if (!TextUtils.isEmpty(authToken)) {
            Timber.d("have a JWT, use it")
            addTokenToResultBundle(account, authToken, result)
        } else {
            Timber.d("no JWT, so go to login")
            tryTokenRefresh(accountAuthenticatorResponse, account, am, result)
        }
        return result
    }

    private fun tryTokenRefresh(accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, am: AccountManager, result: Bundle) {
        val oldToken = am.getUserData(account, AuthConstants.USER_DATA_CURRENT_TOKEN)
        authApi.refreshOtp(oldToken, null).subscribe({ response ->
            if (response.isSuccessful) {
                addTokenToResultBundle(account, response.body()!!.data, result)
            } else {
                userService.logout(false, false).subscribe({ aBoolean -> }, { Timber.d(it) })
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse)
                intent.putExtra(EXTRA_TOKEN_EXPIRED, true)
                result.putParcelable(AccountManager.KEY_INTENT, intent)
            }
        }, { Timber.e(it) })
    }

    private fun addTokenToResultBundle(account: Account, authToken: String?, result: Bundle) {
        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
        result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
    }

    override fun getAuthTokenLabel(authTokenType: String): String? {
        return if (AuthConstants.AUTH_TOKENTYPE == authTokenType) authTokenType else null
    }

    @Throws(NetworkErrorException::class)
    override fun updateCredentials(accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, s: String, bundle: Bundle): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun hasFeatures(accountAuthenticatorResponse: AccountAuthenticatorResponse, account: Account, strings: Array<String>): Bundle? {
        return null
    }

    override fun getAccountRemovalAllowed(response: AccountAuthenticatorResponse, account: Account): Bundle {
        val result = Bundle()
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true)
        return result
    }

    companion object {

        private val EXTRA_NEW_ACCOUNT = "extra_new_account"
        private val EXTRA_TOKEN_EXPIRED = "extra_token_expired"
    }


}
