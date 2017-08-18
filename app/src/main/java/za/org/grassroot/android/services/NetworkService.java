package za.org.grassroot.android.services;

import io.reactivex.Observable;
import za.org.grassroot.android.model.UploadResult;
import za.org.grassroot.android.model.enums.NetworkEntityType;
import za.org.grassroot.android.model.network.EntityForDownload;
import za.org.grassroot.android.model.network.EntityForUpload;

/**
 * Created by luke on 2017/08/16.
 */

public interface NetworkService {

    Observable<UploadResult> uploadEntity(EntityForUpload entityForUpload, boolean forceEvenIfPriorUploaded);

    /**
     *
     * @param entityType The type of entity to download
     * @param forceFullRefresh Whether to refresh entities regardless of whether the server copy is 'newer'
     * @param <E> The dynamic type
     * @return An observable that emits each changed or new entity
     */
    <E extends EntityForDownload> Observable<E> downloadAllChangedOrNewEntities(NetworkEntityType entityType, boolean forceFullRefresh);

    /*<E extends EntityForDownload> void downloadChangedOrNewEntitiesInSet(Set<String> uids,
                                                                         NetworkEntityType entityType,
                                                                         boolean forceFullRefresh);*/

    // <E extends RealmObject> Single<E> downloadEntity(Class<E> clazz, String uid);

}
