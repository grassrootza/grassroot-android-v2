package za.org.grassroot2.services

import io.reactivex.Flowable
import io.reactivex.Observable
import retrofit2.Response
import za.org.grassroot2.model.AroundEntity
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.UploadResult
import za.org.grassroot2.model.alert.LiveWireAlert
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.network.EntityForDownload
import za.org.grassroot2.model.network.EntityForUpload
import za.org.grassroot2.model.request.MemberRequest
import za.org.grassroot2.model.task.Meeting
import za.org.grassroot2.model.task.Task

/**
 * Created by luke on 2017/08/16.
 */

interface NetworkService {

    fun uploadEntity(entityForUpload: EntityForUpload, forceEvenIfPriorUploaded: Boolean): Observable<UploadResult>

    /**
     *
     * @param entityType The type of entity to download
     * @param forceFullRefresh Whether to refresh entities regardless of whether the server copy is 'newer'
     * @param <E> The dynamic type
     * @return An observable that emits each changed or new entity
    </E> */
    fun <E : EntityForDownload> downloadAllChangedOrNewEntities(entityType: GrassrootEntityType, forceFullRefresh: Boolean): Observable<List<E>>

    fun downloadAllChangedOrNewGroups(): Observable<List<Group>>

    fun getTimestampForText(date: String): Observable<Long>

    fun downloadTaskMinimumInfo(): Observable<List<Task>>

    fun inviteContactsToGroup(groupId: String, contacts: List<MemberRequest>): Observable<Response<Void>>

    fun getTasksForGroup(groupId: String): Observable<List<Task>>

    fun getTasksByUids(uids: Map<String, String>): Observable<List<Task>>

    fun createTask(t: Task): Observable<Resource<Task>>

    fun getAlertsAround(longitude: Double, latitude: Double, radius: Int): Observable<List<LiveWireAlert>>

    fun getAllAround(longitude: Double, latitude: Double, radius: Int): Flowable<Resource<List<AroundEntity>>>

    fun respondToMeeting(meetingUid: String, response: String): Observable<Response<Void>>
}
