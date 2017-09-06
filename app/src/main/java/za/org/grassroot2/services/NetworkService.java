package za.org.grassroot2.services;

import java.util.List;

import io.reactivex.Observable;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.UploadResult;
import za.org.grassroot2.model.enums.NetworkEntityType;
import za.org.grassroot2.model.network.EntityForDownload;
import za.org.grassroot2.model.network.EntityForUpload;

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
    <E extends EntityForDownload> Observable<List<E>> downloadAllChangedOrNewEntities(NetworkEntityType entityType, boolean forceFullRefresh);
    Observable<List<Group>> downloadAllChangedOrNewGroups();

}