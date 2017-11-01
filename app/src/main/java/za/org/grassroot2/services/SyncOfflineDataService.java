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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.alert.LiveWireAlert;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.Syncable;
import za.org.grassroot2.model.request.MemberRequest;
import za.org.grassroot2.model.task.Meeting;

/**
 * Created by qbasso on 23.10.2017.
 */

public class SyncOfflineDataService extends Service {

    @Inject DatabaseService dbService;
    @Inject NetworkService  networkService;
    private CompositeDisposable disposabele = new CompositeDisposable();

    @Override
    public void onCreate() {
        super.onCreate();
        ((GrassrootApplication) getApplication()).getAppComponent().inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        disposabele.add(Observable.zip(dbService.getObjectsToSync(Meeting.class),
                dbService.getObjectsToSync(LiveWireAlert.class),
                dbService.getMemberRequestsToSync(),
                (meetings, alerts, memberRequests) -> combineAndSortSyncables(meetings, alerts, memberRequests)).flatMap(syncables -> {
            for (Syncable syncable : syncables) {
                Timber.d("Syncing object: %s", syncable.toString());
                if (syncable instanceof MemberRequest) {
                    handleMemberRequestSync((MemberRequest) syncable);
                } else if (syncable instanceof LiveWireAlert) {
                    handleAlertSync((LiveWireAlert) syncable);
                } else {
                    handleMeetingSync((Meeting) syncable);
                }
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
        disposabele.add(networkService.uploadEntity(syncable, false).subscribe(uploadResult -> {
            if (!TextUtils.isEmpty(uploadResult.getServerUid())) {
                syncable.setServerUid(uploadResult.getServerUid());
                syncable.setSynced(true);
            } else {
                syncable.setSynced(false);
            }
            dbService.storeObject(LiveWireAlert.class, syncable);
        }, throwable -> throwable.printStackTrace()));
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
        networkService.inviteContactsToGroup(syncable.groupUid, Collections.singletonList(syncable)).subscribe(voidResponse -> {
            if (voidResponse.isSuccessful()) {
                dbService.delete(syncable);
            }
        }, Throwable::printStackTrace);
    }

    private void handleMeetingSync(Meeting syncable) {
        if (System.currentTimeMillis() >= syncable.getDeadlineMillis()) {
            Timber.d("Deleting object due to start date in the past");
            dbService.delete(Meeting.class, syncable);
            EventBus.getDefault().post(new ObjectOutOfSyncEvent(syncable));
        }
        networkService.createTask(syncable).subscribe(taskResource -> dbService.delete(Meeting.class, syncable), Throwable::printStackTrace);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposabele.dispose();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class ObjectOutOfSyncEvent {
        public final Meeting syncable;

        public ObjectOutOfSyncEvent(Meeting syncable) {
            this.syncable = syncable;
        }
    }
}
