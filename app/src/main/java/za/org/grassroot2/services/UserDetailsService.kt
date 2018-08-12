package za.org.grassroot2.services

import android.accounts.Account

import io.reactivex.Single
import za.org.grassroot2.model.UserProfile

/**
 * Created by luke on 2017/08/09.
 */

interface UserDetailsService {

    val isSyncCompleted: Boolean

    val isSyncFailed: Boolean

    val currentToken: String?
    val currentUserUid: String?
    val currentUserMsisdn: String?

    fun storeUserDetails(userUid: String,
                         userPhone: String,
                         userDisplayName: String,
                         userEmailAddress: String?,
                         userLanguageCode: String,
                         userSystemRole: String?,
                         userToken: String): Single<UserProfile>

    fun setAuthToken(userToken: String): Account

    fun logout(deleteAndroidAccount: Boolean, wipeDb: Boolean): Single<Boolean>

    fun setSyncState(status: Int)

    fun requestSync()

}