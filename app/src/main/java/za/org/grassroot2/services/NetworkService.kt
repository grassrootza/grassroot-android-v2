package za.org.grassroot2.services

import io.reactivex.Flowable
import io.reactivex.Observable
import retrofit2.Response
import za.org.grassroot2.model.*
import za.org.grassroot2.model.alert.LiveWireAlert
import za.org.grassroot2.model.enums.GrassrootEntityType
import za.org.grassroot2.model.language.NluResponse
import za.org.grassroot2.model.network.EntityForDownload
import za.org.grassroot2.model.network.EntityForUpload
import za.org.grassroot2.model.request.MemberRequest
import za.org.grassroot2.model.task.PendingResponseDTO
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.model.task.Vote
import za.org.grassroot2.model.task.Todo

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

    fun downloadCompleteGroupInfo(groupId: String): Observable<Group>

    fun getTimestampForText(date: String): Observable<Long>

    fun downloadTaskMinimumInfo(): Observable<List<Task>>

    fun inviteContactsToGroup(groupId: String, contacts: List<MemberRequest>): Observable<Response<Void>>

    fun hideGroup(group: Group): Observable<Boolean>

    fun leaveGroup(group: Group): Observable<Boolean>

    fun downloadMemberFile(group: Group): Observable<ByteArray>

    fun getTasksForGroup(groupId: String): Observable<List<Task>>

    fun getTasksByUids(uids: Map<String, String>): Observable<List<Task>>

    fun createTask(t: Task): Observable<Resource<Task>>

    fun getAlertsAround(longitude: Double, latitude: Double, radius: Int): Observable<List<LiveWireAlert>>

    fun getAllAround(longitude: Double, latitude: Double, radius: Int): Flowable<Resource<List<AroundEntity>>>

    fun respondToMeeting(meetingUid: String, response: String): Observable<Response<Void>>

    fun respondToTodo(todoUid: String, response: String): Observable<Todo>

    fun respondToVote(voteUid: String, response: String): Observable<Vote>

    fun uploadMeetingPost(meetingUid: String, description: String, mediaFile: MediaFile?): Observable<Response<Void>>

    fun seekIntentInText(text: String): Flowable<NluResponse>

    fun uploadSpeech(sampleRate: Int, parseForIntent: Boolean, filePath: String): Observable<Response<Void>>

    fun getMeetingPosts(taskUid: String): Flowable<Resource<List<Post>>>

    fun fetchTodoResponses(taskUid: String): Observable<Map<String, String>>

    fun downloadTodoResponses(taskUid: String): Observable<ByteArray>?

    fun fetchPendingResponses(): Observable<PendingResponseDTO>

    fun createGroup(group: Group): Observable<Group>

    //fun getTodoPosts(taskUid: String): Flowable<Resource<List<Posts>>>
}
