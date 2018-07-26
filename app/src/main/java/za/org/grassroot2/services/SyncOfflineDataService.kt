package za.org.grassroot2.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.text.TextUtils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import za.org.grassroot2.GrassrootApplication
import za.org.grassroot2.R
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.alert.LiveWireAlert
import za.org.grassroot2.model.exception.ServerErrorException
import za.org.grassroot2.model.network.Syncable
import za.org.grassroot2.model.request.MemberRequest
import za.org.grassroot2.model.task.Meeting
import java.util.ArrayList
import javax.inject.Inject
import kotlin.Comparator
import kotlin.Exception
import kotlin.Int
import kotlin.String

/**
 * Created by qbasso on 23.10.2017.
 */

class SyncOfflineDataService : Service() {

    @Inject internal lateinit var dbService: DatabaseService
    @Inject internal lateinit var networkService: NetworkService

    private val disposable = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        (application as GrassrootApplication).appComponent.inject(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val meetingObservable: Observable<List<Meeting>> = dbService.getObjectsToSync(Meeting::class.java)
        val alertObservable: Observable<List<LiveWireAlert>> = dbService.getObjectsToSync(LiveWireAlert::class.java)
        val memberRequestObservable: Observable<List<MemberRequest>> = dbService.getMemberRequestsToSync()

        val combinedObservable = Observable.zip<List<Meeting>, List<LiveWireAlert>, List<MemberRequest>, List<Syncable>>(
                meetingObservable, alertObservable, memberRequestObservable, Function3 { meetings: List<Meeting>, alerts: List<LiveWireAlert>, requests: List<MemberRequest> ->
                    combineAndSortSyncables(meetings, alerts, requests)});

        disposable.add(combinedObservable
                .flatMapIterable { syncables -> syncables }
                .flatMap { syncable ->
                    Timber.d("Syncing object: %s", syncable.toString())
                    if (syncable is MemberRequest) {
                        handleMemberRequestSync(syncable)
                    } else if (syncable is LiveWireAlert) {
                        handleAlertSync(syncable)
                    } else {
                        handleMeetingSync(syncable as Meeting)
                    }
                    Observable.just(true) }
                .subscribeOn(Schedulers.single())
                .subscribe({ o -> stopSelf() }, { throwable ->
                    Timber.d("Object not synced")
                    throwable.printStackTrace()
                    stopSelf()
                }))
        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleAlertSync(syncable: LiveWireAlert) {
        val result = networkService.uploadEntity(syncable, false).blockingFirst()
        if (result.uploadException is ServerErrorException) {
            dbService.delete(LiveWireAlert::class.java, syncable)
            postFailureMessage(syncable)
        } else if (!TextUtils.isEmpty(result.serverUid)) {
            syncable.serverUid = result.serverUid
            syncable.isSynced = true
            dbService.storeObject(LiveWireAlert::class.java, syncable)
        } else {
            syncable.isSynced = false
            dbService.storeObject(LiveWireAlert::class.java, syncable)
        }
    }

    private fun postFailureMessage(syncable: Syncable) {
        EventBus.getDefault().post(ObjectOutOfSyncEvent(syncable, buildErrorMessage(syncable)))
    }

    @SafeVarargs
    private fun combineAndSortSyncables(vararg args: List<Syncable>): List<Syncable> {
        val result = ArrayList<Syncable>()
        for (l in args) {
            result.addAll(l)
        }
        result.sortWith(Comparator { o1, o2 -> if (o1.createdDate() > o2.createdDate()) 1 else if (o1.createdDate() < o2.createdDate()) -1 else 0 })
        return result
    }

    private fun handleMemberRequestSync(syncable: MemberRequest) {
        val response = networkService.inviteContactsToGroup(syncable.groupUid, listOf(syncable)).blockingFirst()
        if (!response.isSuccessful) {
            postFailureMessage(syncable)
        }
        dbService.delete(syncable)
    }

    @Throws(Exception::class)
    private fun handleMeetingSync(syncable: Meeting) {
        if (System.currentTimeMillis() >= syncable.deadlineMillis) {
            Timber.d("Deleting object due to start date in the past")
            dbService.delete(Meeting::class.java, syncable)
            postFailureMessage(syncable)
        }
        val task = networkService.createTask(syncable).blockingFirst()
        if (task.status == Status.SUCCESS) {
            dbService.delete(Meeting::class.java, syncable)
        } else if (task.status == Status.SERVER_ERROR) {
            dbService.delete(Meeting::class.java, syncable)
            postFailureMessage(syncable)
        } else if (task.status == Status.ERROR) {
            throw Exception(task.message)
        }
    }

    private fun buildErrorMessage(syncable: Syncable): String {
        return if (syncable is MemberRequest) {
            getString(R.string.invite_failed, syncable.displayName)
        } else if (syncable is LiveWireAlert) {
            getString(R.string.alert_failed, syncable.headline)
        } else {
            if (System.currentTimeMillis() >= (syncable as Meeting).deadlineMillis) {
                getString(R.string.meeting_out_of_sync, syncable.name)
            } else {
                getString(R.string.meeting_failed, syncable.name)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    class ObjectOutOfSyncEvent(val syncable: Syncable, val msg: String?)
}
