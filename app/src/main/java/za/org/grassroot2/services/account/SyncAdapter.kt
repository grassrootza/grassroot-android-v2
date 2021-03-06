package za.org.grassroot2.services.account

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.OperationApplicationException
import android.content.SyncResult
import android.os.Bundle
import android.os.RemoteException
import android.support.annotation.RequiresApi

import org.greenrobot.eventbus.EventBus
import org.json.JSONException

import java.io.IOException
import java.util.HashMap

import javax.inject.Inject

import timber.log.Timber
import za.org.grassroot2.GrassrootApplication
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.UserProfile
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.exception.ServerUnreachableException
import za.org.grassroot2.model.network.EntityForDownload
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.services.UserDetailsService

class SyncAdapter : AbstractThreadedSyncAdapter {

    lateinit var networkService: NetworkService
        @Inject set

    lateinit var databaseService: DatabaseService
        @Inject set

    lateinit var userDetailsService: UserDetailsService
        @Inject set

    constructor(context: Context, autoInitialize: Boolean) : super(context, autoInitialize) {
        initDagger()
    }

    @RequiresApi(11)
    constructor(context: Context, autoInitialize: Boolean, parallel: Boolean) : super(context, autoInitialize, parallel) {
        initDagger()
    }

    private fun initDagger() {
        Timber.d("created sync adapter ...")
        (context.applicationContext as GrassrootApplication).appComponent.inject(this)
    }

    /**
     * This method is run by the Android framework, on a new Thread, to perform a sync.
     *
     * @param account    Current account
     * @param extras     Bundle extras
     * @param authority  Content authority
     * @param provider   [ContentProviderClient]
     * @param syncResult Object to write stats to
     */
    override fun onPerformSync(account: Account, extras: Bundle, authority: String, provider: ContentProviderClient, syncResult: SyncResult) {
        Timber.d("Starting synchronization...")
        networkService.downloadAllChangedOrNewEntities<EntityForDownload>(GrassrootEntityType.GROUP, false)
                .subscribe({ entityForDownloads ->
                    databaseService.copyOrUpdateListOfEntities(Group::class.java,
                            convert(entityForDownloads))
                }) { throwable -> handleSyncError(syncResult, throwable) }
        networkService.downloadTaskMinimumInfo().flatMap { tasksMin ->
            databaseService.storeTasks(tasksMin)
            val uids = HashMap<String, String>()
            for (t in tasksMin) {
                uids.put(t.uid, t.type.name)
            }
            networkService.getTasksByUids(uids)
        }.subscribe({ tasksFull ->
            databaseService.storeTasks(tasksFull)
            userDetailsService.setSyncState(UserProfile.SYNC_STATE_COMPLETED)
            EventBus.getDefault().post(SyncCompletedEvent())
        }) { throwable -> handleSyncError(syncResult, throwable) }
    }

    private fun handleSyncError(syncResult: SyncResult, throwable: Throwable) {
        userDetailsService.setSyncState(UserProfile.SYNC_STATE_FAILED)
        EventBus.getDefault().postSticky(SyncCompletedEvent())
        throwable.printStackTrace()
        if (throwable is IOException || throwable is ServerUnreachableException) {
            Timber.e(throwable, "Error synchronizing!")
            syncResult.stats.numIoExceptions++
        } else if (throwable is JSONException) {
            Timber.e(throwable, "Error synchronizing!")
            syncResult.stats.numParseExceptions++
        } else if (throwable is RemoteException || throwable is OperationApplicationException) {
            Timber.e(throwable, "Error synchronizing!")
            syncResult.stats.numAuthExceptions++
        }
    }

    private fun convert(entityForDownloads: List<EntityForDownload>): List<Group> {
        return entityForDownloads.map { it as Group }
    }

    class SyncCompletedEvent
}
