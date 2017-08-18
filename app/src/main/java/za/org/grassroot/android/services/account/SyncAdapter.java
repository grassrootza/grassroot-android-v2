package za.org.grassroot.android.services.account;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import javax.inject.Inject;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.realm.RealmObject;
import timber.log.Timber;
import za.org.grassroot.android.GrassrootApplication;
import za.org.grassroot.android.dagger.user.ApiModule;
import za.org.grassroot.android.model.enums.NetworkEntityType;
import za.org.grassroot.android.model.network.EntityForDownload;
import za.org.grassroot.android.services.NetworkService;
import za.org.grassroot.android.services.RealmService;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private SyncResult syncResult;

    private NetworkService networkService;
    private RealmService realmService;

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
        Timber.e("created sync adapter ...");
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
    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
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
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Timber.d("Starting synchronization...");
        networkService.downloadAllChangedOrNewEntities(NetworkEntityType.GROUP, false)
            .subscribe(new Consumer<EntityForDownload>() {
                @Override
                public void accept(@NonNull EntityForDownload entityForDownload) throws Exception {
                    Timber.v("got a group");
                    realmService.storeRealmObject((RealmObject) entityForDownload, false);
                }
            });

        /*try {
        } catch (IOException ex) {
            Timber.e(ex, "Error synchronizing!");
            syncResult.stats.numIoExceptions++;
        } catch (JSONException ex) {
            Timber.e(ex, "Error synchronizing!");
            syncResult.stats.numParseExceptions++;
        } catch (RemoteException |OperationApplicationException ex) {
            Timber.e(ex, "Error synchronizing!");
            syncResult.stats.numAuthExceptions++;
        }

        Timber.d("Finished synchronization!");*/
    }
}
