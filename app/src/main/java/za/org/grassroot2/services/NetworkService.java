package za.org.grassroot2.services;

import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import retrofit2.Response;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.UploadResult;
import za.org.grassroot2.model.contact.Contact;
import za.org.grassroot2.model.enums.GrassrootEntityType;
import za.org.grassroot2.model.network.EntityForDownload;
import za.org.grassroot2.model.network.EntityForUpload;
import za.org.grassroot2.model.task.Task;

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
    <E extends EntityForDownload> Observable<List<E>> downloadAllChangedOrNewEntities(GrassrootEntityType entityType, boolean forceFullRefresh);
    Observable<List<Group>> downloadAllChangedOrNewGroups();

    Observable<Long> getTimestampForText(String date);

    Observable<List<Task>> downloadTaskMinimumInfo();

    Observable<Response<Void>> inviteContactsToGroup(String groupId, List<Contact> contacts);

    Observable<List<Task>> getTasksForGroup(String groupId);

    Observable<List<Task>> getTasksByUids(Map<String, String> uids);

    Flowable<Resource<Task>> createTask(Task t);
}
