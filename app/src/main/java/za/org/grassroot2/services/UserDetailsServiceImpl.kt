package za.org.grassroot2.services

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.os.Bundle
import io.reactivex.Single
import timber.log.Timber
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.UserProfile
import za.org.grassroot2.services.account.AuthConstants
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserDetailsServiceImpl @Inject
constructor(private val accountManager: AccountManager, private val databaseService: DatabaseService) : UserDetailsService {

    override val isSyncCompleted: Boolean
        get() {
            val userProfile = databaseService.loadUserProfile()
            return userProfile != null && userProfile.isSyncComplete
        }

    override val isSyncFailed: Boolean
        get() {
            val userProfile = databaseService.loadUserProfile()
            return userProfile != null && userProfile.isSyncFailed
        }

    override val currentToken: String?
        get() {
            val account = account
            return if (account == null) null else accountManager.peekAuthToken(account, AuthConstants.AUTH_TOKENTYPE)
        }

    private val orCreateAccount: Account
        get() {
            Timber.d("getting or creating account for Grassroot ...")
            val accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE)
            Timber.d("number of accounts: %s", accounts.size)
            val account: Account
            if (accounts.isNotEmpty()) {
                account = accounts[0]
            } else {
                account = Account(AuthConstants.ACCOUNT_NAME, AuthConstants.ACCOUNT_TYPE)
                Timber.d("adding account explicitly")
                accountManager.addAccountExplicitly(account, null, null)
                Timber.d("setting account as syncable")
            }
            return account
        }

    private val account: Account?
        get() {
            val accounts = accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE)
            return if (accounts.size == 0) null else accounts[0]
        }


    override// todo: throw an error if this is null, which should trigger a user logout
    val currentUserUid: String?
        get() {
            val userProfile = databaseService.loadUserProfile()
            return userProfile?.uid
        }

    override val currentUserMsisdn: String?
        get() {
            val userProfile = databaseService.loadUserProfile()
            return userProfile?.msisdn
        }

    override fun storeUserDetails(userUid: String,
                                  userPhone: String,
                                  userDisplayName: String,
                                  userEmailAddress: String,
                                  userLanguageCode: String,
                                  userSystemRole: String?,
                                  userToken: String): Single<UserProfile> {
        return Single.create { e ->
            val account = setAuthToken(userToken)
            Timber.v("stored auth token, number accounts = %s", accountManager.getAccountsByType(AuthConstants.ACCOUNT_TYPE).size)
            val userProfile = databaseService.updateOrCreateUserProfile(userUid, userPhone, userDisplayName, userEmailAddress, userLanguageCode, userSystemRole)
            setAccountAsSyncable(account)
            e.onSuccess(userProfile)
        }
    }

    override fun setAuthToken(userToken: String): Account {
        val account = orCreateAccount
        accountManager.setAuthToken(account, AuthConstants.AUTH_TOKENTYPE, userToken)
        accountManager.setUserData(account, AuthConstants.USER_DATA_CURRENT_TOKEN, userToken)
        accountManager.setUserData(account, AuthConstants.USER_DATA_LOGGED_IN, "true")
        return account
    }

    // todo: disposableOnDetach exception handling, also calls to server, GCM, etc
    override fun logout(deleteAndroidAccount: Boolean, wipeDb: Boolean): Single<Boolean> {
        return Single.create { e ->
            // first, wipe the details stored in account
            val account = account
            if (account != null) {
                ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 0)
                accountManager.invalidateAuthToken(AuthConstants.ACCOUNT_TYPE,
                        accountManager.peekAuthToken(account, AuthConstants.AUTH_TOKENTYPE))
                accountManager.setPassword(account, null)
                accountManager.setUserData(account, AuthConstants.USER_DATA_CURRENT_TOKEN, null)
                accountManager.setUserData(account, AuthConstants.USER_DATA_LOGGED_IN, null)
                if (deleteAndroidAccount) {
                    // using deprecated because non-deprecated requires API22+ .. oh Android
                    accountManager.removeAccount(account, null, null)
                }
            }
            databaseService.removeUserProfile() // then, wipe the UID etc
            if (wipeDb) {
                databaseService.wipeDatabase()
            }
            e.onSuccess(true)
        }
    }

    override fun setSyncState(status: Int) {
        val userProfile = databaseService.loadUserProfile()
        if (status == UserProfile.SYNC_STATE_FAILED && userProfile!!.syncStatus != UserProfile.SYNC_STATE_NONE) {
            return
        } else {
            userProfile!!.setSyncState(status)
            databaseService.storeObject(UserProfile::class.java, userProfile)
        }
    }

    // warehousing
    private fun setAccountAsSyncable(account: Account) {
        val authority = CONTENT_AUTHORITY
        val pollFrequency = TimeUnit.MINUTES.toMillis(15)

        // Inform the system that this account supports sync
        ContentResolver.setIsSyncable(account, authority, 1)

        // Inform the system that this account is eligible for auto sync when the network is up
        ContentResolver.setSyncAutomatically(account, authority, true)

        // Recommend a schedule for automatic synchronization. The system may modify this based
        // on other scheduled syncs and network utilization.
        ContentResolver.addPeriodicSync(account, authority, Bundle(), pollFrequency)
    }

    override fun requestSync() {
        Timber.d("requesting a sync ...")
        val b = Bundle()
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
        ContentResolver.requestSync(account, CONTENT_AUTHORITY, b)

    }

    companion object {
        private const val CONTENT_AUTHORITY = "za.org.grassroot2.syncprovider"
    }
}
