package za.org.grassroot2.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.R;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.UploadResult;
import za.org.grassroot2.model.alert.LiveWireAlert;
import za.org.grassroot2.model.exception.ServerErrorException;
import za.org.grassroot2.model.network.Syncable;
import za.org.grassroot2.model.request.MemberRequest;
import za.org.grassroot2.model.task.Meeting;
import za.org.grassroot2.model.task.Task;

/**
 * Created by qbasso on 23.10.2017.
 */

public class SyncOfflineDataService extends Service {

    @Inject DatabaseService dbService;
    @Inject NetworkService  networkService;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public void onCreate() {
        super.onCreate();
        ((GrassrootApplication) getApplication()).getAppComponent().inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        disposable.add(Observable.zip(dbService.getObjectsToSync(Meeting.class),
                dbService.getObjectsToSync(LiveWireAlert.class),
                dbService.getMemberRequestsToSync(),
                (meetings, alerts, memberRequests) -> combineAndSortSyncables(meetings, alerts, memberRequests)).flatMapIterable(syncables -> syncables).flatMap(syncable -> {
            Timber.d("Syncing object: %s", syncable.toString());
            if (syncable instanceof MemberRequest) {
                handleMemberRequestSync((MemberRequest) syncable);
            } else if (syncable instanceof LiveWireAlert) {
                handleAlertSync((LiveWireAlert) syncable);
            } else {
                handleMeetingSync((Meeting) syncable);
            }
            return Observable.just(true);
        }).subscribeOn(Schedulers.single()).subscribe(o -> stopSelf(), throwable -> {
            Timber.d("Object not synced");
            throwable.printStackTrace();
            stopSelf();
        }));
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleAlertSync(LiveWireAlert syncable) {
        UploadResult result = networkService.uploadEntity(syncable, false).blockingFirst();
        if (result.getUploadException() instanceof ServerErrorException) {
            dbService.delete(LiveWireAlert.class, syncable);
            postFailureMessage(syncable);
        } else if (!TextUtils.isEmpty(result.getServerUid())) {
            syncable.setServerUid(result.getServerUid());
            syncable.setSynced(true);
            dbService.storeObject(LiveWireAlert.class, syncable);
        } else {
            syncable.setSynced(false);
            dbService.storeObject(LiveWireAlert.class, syncable);
        }
    }

    private void postFailureMessage(Syncable syncable) {
        EventBus.getDefault().post(new ObjectOutOfSyncEvent(syncable, buildErrorMessage(syncable)));
    }

    @SafeVarargs
    @NonNull
    private final List<Syncable> combineAndSortSyncables(List<? extends Syncable>... args) {
        List<Syncable> result = new ArrayList<>();
        for (List<? extends Syncable> l : args) {
            result.addAll(l);
        }
        Collections.sort(result, (o1, o2) -> o1.createdDate() > o2.createdDate() ? 1 : (o1.createdDate() < o2.createdDate() ? -1 : 0));
        return result;
    }

    private void handleMemberRequestSync(MemberRequest syncable) {
        Response<Void> response = networkService.inviteContactsToGroup(syncable.groupUid, Collections.singletonList(syncable)).blockingFirst();
        if (!response.isSuccessful()) {
            postFailureMessage(syncable);
        }
        dbService.delete(syncable);
    }

    private void handleMeetingSync(Meeting syncable) throws Exception {
        if (System.currentTimeMillis() >= syncable.getDeadlineMillis()) {
            Timber.d("Deleting object due to start date in the past");
            dbService.delete(Meeting.class, syncable);
            postFailureMessage(syncable);
        }
        Resource<Task> task = networkService.createTask(syncable).blockingFirst();
        if (task.status == Status.SUCCESS) {
            dbService.delete(Meeting.class, syncable);
        } else if (task.status == Status.SERVER_ERROR) {
            dbService.delete(Meeting.class, syncable);
            postFailureMessage(syncable);
        } else if (task.status == Status.ERROR){
            throw new Exception(task.message);
        }
    }

    private String buildErrorMessage(Syncable syncable) {
        if (syncable instanceof MemberRequest) {
            return getString(R.string.invite_failed, ((MemberRequest) syncable).displayName);
        } else if (syncable instanceof LiveWireAlert) {
            return getString(R.string.alert_failed, ((LiveWireAlert) syncable).getHeadline());
        } else {
            if (System.currentTimeMillis() >= ((Meeting)syncable).getDeadlineMillis()) {
                return getString(R.string.meeting_out_of_sync, ((Meeting) syncable).getName());
            } else {
                return getString(R.string.meeting_failed, ((Meeting) syncable).getName());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class ObjectOutOfSyncEvent {
        public final Syncable syncable;
        @Nullable
        public final String   msg;

        public ObjectOutOfSyncEvent(Syncable syncable, @Nullable String msg) {
            this.syncable = syncable;
            this.msg = msg;
        }
    }
}
