package za.org.grassroot2.services.account;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import za.org.grassroot2.GrassrootApplication;
import za.org.grassroot2.dagger.user.ApiModule;
import za.org.grassroot2.database.DatabaseService;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.exception.ServerUnreachableException;
import za.org.grassroot2.model.network.EntityForDownload;
import za.org.grassroot2.services.NetworkService;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private NetworkService networkService;
    private DatabaseService databaseService;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        initDagger();
    }

    @RequiresApi(11)
    public SyncAdapter(Context context, boolean autoInitialize, boolean parallel) {
        super(context, autoInitialize, parallel);
        initDagger();
    }

    private void initDagger() {
        Timber.d("created sync adapter ...");
        ((GrassrootApplication) (getContext().getApplicationContext()))
                .getAppComponent()
                .plus(new ApiModule())
                .inject(this);
    }

    @Inject
    public void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }

    @Inject
    public void setDatabaseService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    /**
     * This method is run by the Android framework, on a new Thread, to perform a sync.
     * @param account Current account
     * @param extras Bundle extras
     * @param authority Content authority
     * @param provider {@link ContentProviderClient}
     * @param syncResult Object to write stats to
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, final SyncResult syncResult) {
        Timber.d("Starting synchronization...");
        networkService.downloadAllChangedOrNewEntities(GrassrootEntityType.GROUP, false)
            .subscribe(entityForDownloads ->
                databaseService.copyOrUpdateListOfEntities(Group.class, convert(entityForDownloads)),
                    throwable -> {
                if (throwable instanceof IOException || throwable instanceof ServerUnreachableException) {
                    Timber.e(throwable, "Error synchronizing!");
                    syncResult.stats.numIoExceptions++;
                } else if (throwable instanceof JSONException) {
                    Timber.e(throwable, "Error synchronizing!");
                    syncResult.stats.numParseExceptions++;
                } else if (throwable instanceof RemoteException ||
                        throwable instanceof OperationApplicationException) {
                    Timber.e(throwable, "Error synchronizing!");
                    syncResult.stats.numAuthExceptions++;
                }
            });
    }

    // multiple bounds are not playing nice with the observable, so using this slight hack
    // todo: figure out and fix multiple bounds on method above
    private List<Group> convert(List<EntityForDownload> entityForDownloads) {
        List<Group> list = new ArrayList<>();
        for (EntityForDownload e : entityForDownloads) {
            list.add((Group) e);
        }
        return list;
    }
}
