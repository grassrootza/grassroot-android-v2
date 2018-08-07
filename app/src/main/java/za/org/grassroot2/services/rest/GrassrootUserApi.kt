package za.org.grassroot2.services.rest

import io.reactivex.Flowable
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response

import retrofit2.http.*
import za.org.grassroot2.model.*
import za.org.grassroot2.model.alert.LiveWireAlert
import za.org.grassroot2.model.language.NluResponse
import za.org.grassroot2.model.request.MemberRequest
import za.org.grassroot2.model.task.PendingResponseDTO
import za.org.grassroot2.model.task.Task
import za.org.grassroot2.model.task.Todo
import za.org.grassroot2.model.task.Vote

interface GrassrootUserApi {

    // Fetching groups
    @POST("/v2/api/group/fetch/updated")
    fun fetchUserGroups(@Body existingUids: Map<String, Long>): Observable<List<Group>>

    @GET("/v2/api/group/fetch/info")
    fun fetchGroupsInfo(@Query("groupUids") groupUids: List<String>): Observable<List<Group>>

    @GET("/v2/api/group/fetch/full")
    fun fetchFullGroupInfo(@Query("groupUid") groupUid: String): Observable<Group>

    // Send a media file to the server for storage
    @Multipart
    @POST("/v2/api/media/store")
    fun sendMediaFile(@Query("imageKey") fileUid: String,
                      @Query("mediaFunction") function: String,
                      @Query("mimeType") mimeType: String,
                      @Part file: MultipartBody.Part?): Observable<Response<RestResponse<String>>>

    // Create a LiveWire alert

    @POST("/v2/api/livewire/create")
    fun createLiveWireAlert(@Query("headline") headline: String,
                            @Query("description") description: String,
                            @Query("type") type: String,
                            @Query("groupUid") groupUid: String,
                            @Query("taskUid") taskUid: String,
                            @Query("addLocation") addLocation: Boolean,
                            @Query("latitude") latitude: Double,
                            @Query("longitude") longitude: Double,
                            @Query("mediaFileKeys") mediaFileUids: Set<String>): Observable<Response<RestResponse<String>>>

    @GET("/v2/api/task/fetch/todo/responses/{taskUid}")
    fun fetchTodoResponses(@Path("taskUid") taskUid: String): Observable<Map<String, String>>

    @GET("/v2/api/task/fetch/todo/download/{taskUid}")
    fun downloadTodoResponses(@Path("taskUid") taskUid: String): Observable<ByteArray>

    @GET("/v2/api/task/respond/todo/information/{todoUid}")
    fun respondToTodo(@Path("todoUid") taskUid: String,
                      @Query("response") response: String): Observable<Response<Todo>>

    @POST("/v2/api/task/fetch/updated/group/{groupUid}")
    fun fetchGroupTasksMinimumInfo(@Path("groupUid") groupUid: String, @Body timestamps: Map<String, Long>): Observable<List<Task>>

    @GET("/v2/api/user/pending")
    fun fetchPendingResponses(): Observable<PendingResponseDTO>

    @POST("/v2/api/task/fetch/specified")
    fun fetchTasksByUid(@Body taskUids: Map<String, String>): Observable<List<Task>>

    @POST("/v2/api/task/fetch/updated")
    fun fetchUserTasksMinimumInfo(@Body timestamps: Map<String, Long>): Observable<List<Task>>

    @POST("/v2/api/group/modify/members/add/{groupUid}")
    fun addMembersToGroup(@Path("groupUid") groupId: String, @Body request: List<MemberRequest>): Observable<Response<Void>>

    @POST("/v2/api/group/modify/hide/{groupUid}")
    fun hideGroup(@Path("groupUid") groupUid: String): Observable<Response<Void>>

    @POST("/v2/api/group/modify/leave/{groupUid}")
    fun leaveGroup(@Path("groupUid") groupUid: String): Observable<RestResponse<Boolean>>


    @Multipart
    @POST("/v2/api/group/modify/image/upload/{groupUid}")
    fun uploadGroupProfilePhoto(@Path("groupUid") groupUid: String?,
                                @Part image: MultipartBody.Part?): Observable<Response<MediaUploadResult>>

    // not bothering to stream since the XLS generated is tiny (< 20kb for large groups)
    @GET("/v2/api/group/fetch/export/{groupUid}")
    fun fetchGroupMemberSheet(@Path("groupUid") groupUid: String): Observable<Response<ResponseBody>>

    @GET("/v2/api/language/parse/datetime/text")
    fun getTimestampForTextDate(@Query("text") text: String): Observable<Long>

    @GET("/v2/api/language/list")
    fun fetchLanguages(): Observable<Map<String, String>>

    @POST("/v2/api/task/create/meeting/{parentType}/{parentUid}")
    fun createMeeting(@Path("parentType") parentType: String,
                      @Path("parentUid") parentUid: String,
                      @Query("subject") subject: String,
                      @Query("location") location: String,
                      @Query("dateTimeEpochMillis") created: Long,
                      @Query("description") description: String,
                      @Query("publicMeeting") publicMeeting: Boolean,
                      @Query("userLat") userLatitude: Double,
                      @Query("userLong") userLongitude: Double,
                      @Query("assignedMembersUids") assignedMembersUids: List<String>,
                      @Query("mediaFileUid") mediaFileUid: String): Observable<Response<Task>>

    @POST("/v2/api/task/create/vote/{parentType}/{parentUid}")
    fun createVote(@Path("parentType") parentType: String,
                   @Path("parentUid") parentUid: String,
                   @Query("title") subject: String,
                   @Query("voteOptions") voteOptions: List<String>,
                   @Query("description") description: String,
                   @Query("time") time: Long,
                   @Query("mediaFileUid") mediaFileUid: String,
                   @Query("assignedMemberUids") assignedMemberUids: List<String>): Observable<Response<Task>>

    @POST("/v2/api/task/create/todo/action/{parentType}/{parentUid}")
    fun createActionTodo(@Path("parentType") parentType: String,
                         @Path("parentUid") parentUid: String,
                         @Query("subject") subject: String,
                         @Query("dueDateTime") dueDateTime: Long,
                         @Query("recurring") recurring: Boolean,
                         @Query("recurringPeriodMillis") recurringPeriodMillis: Long,
                         @Query("assignedMembersUids") assignedMembersUids: List<String>,
                         @Query("mediaFileUids") mediaFileUids: List<String>): Observable<Response<Task>>

    @POST("/v2/api/task/create/todo/information/{parentType}/{parentUid}")
    fun createInformationTodo(@Path("parentType") parentType: String,
                              @Path("parentUid") parentUid: String,
                              @Query("subject") subject: String,
                              @Query("responseTag") responseTag: String,
                              @Query("dueDateTime") dueDatetime: Long,
                              @Query("assignedUids") assignedUids: List<String>,
                              @Query("mediaFileUids") mediaFileUids: List<String>): Observable<Response<Task>>

    @POST("/v2/api/task/create/todo/confirmation/{parentType}/{parentUid}")
    fun createConfirmationTodo(@Path("parentType") parentType: String,
                               @Path("parentUid") parentUid: String,
                               @Query("subject") subject: String,
                               @Query("dueDateTime") dueDateTime: Long,
                               @Query("requireImages") requireImages: Boolean,
                               @Query("assignedMemberUids") assignedMemberUids: List<String>,
                               @Query("confirmingMemberUids") confirmingMemberUids: List<String>,
                               @Query("recurring") recurring: Boolean,
                               @Query("recurringPeriodMillis") recurringPeriodMillis: Long,
                               @Query("mediaFileUids") mediaFileUids: List<String>): Observable<Response<Task>>

    @POST("/v2/api/task/create/todo/volunteer/{parentType}/{parentUid}")
    fun createVolunteerTodo(@Path("parentType") parentType: String,
                            @Path("parentUid") parentUid: String,
                            @Query("subject") subject: String,
                            @Query("dueDateTime") dueDateTime: Long,
                            @Query("assignedMemberUids") assignedMemberUids: List<String>,
                            @Query("mediaFileUids") mediaFileUids: List<String>): Observable<Response<Task>>

    @POST("/v2/api/group/modify/create")
    fun createGroup(@Query("name") groupName: String,
                    @Query("description") description: String,
                    @Query("permissionTemplate") permissionTemplate: String,
                    @Query("reminderMinutes") reminderMinutes: Int,
                    @Query("discoverable") discoverable: Boolean,
                    @Query("defaultAddToAccount") defaultAddToAccount: Boolean,
                    @Query("pinGroup") pinGroup: Boolean): Observable<Response<Group>>


    @GET("/v2/api/location/all/alerts")
    fun getAlertsAround(@Query("longitude") longitude: Double,
                        @Query("latitude") latitude: Double,
                        @Query("radiusMetres") radius: Int): Observable<List<LiveWireAlert>>

    @GET("/v2/api/location/all")
    fun getAllAround(@Query("longitude") longitude: Double,
                     @Query("latitude") latitude: Double,
                     @Query("radiusMetres") radius: Int,
                     @Query("saerchType") serachType: String): Observable<List<AroundEntity>>


    @Multipart
    @POST("/v2/api/user/profile/image/change")
    fun uploadProfilePhoto(@Part file: MultipartBody.Part?): Observable<Response<RestResponse<String>>>

    @POST("/v2/api/user/profile/data/update")
    fun updateProfileData(
            @Query("displayName") displayName: String,
            @Query("phoneNumber") phoneNumber: String,
            @Query("email") email: String,
            @Query("languageCode") languageCode: String): Observable<RestResponse<TokenResponse>>


    @POST("/v2/api/task/respond/meeting/{taskUid}")
    fun respondToMeeting(@Path("taskUid") taskUid: String, @Query("response") response: String): Observable<Response<Void>>

    @POST("/v2/api/task/respond/vote/{taskUid}")
    fun respondToVote(@Path("taskUid") taskUid: String, @Query("vote") vote: String): Observable<Response<Vote>>

    @Multipart
    @POST("/v2/api/task/respond/post/{taskType}/{taskUid}")
    fun uploadPost(@Path("taskType") taskType: String,
                   @Path("taskUid") taskUid: String,
                   @Query("caption") title: String,
                   @Part file: MultipartBody.Part?): Observable<Response<Void>>

    @GET("/v2/api/language/parse/intent")
    fun seekIntentInText(@Query("text") text: String): Flowable<NluResponse>

    @Multipart
    @POST("/v2/api/language/parse/speech")
    fun uploadSpeech(@Query("sampleRate") sampleRate: Int?,
                     @Query("parseForIntent") parseForIntent: Boolean,
                     @Part file: MultipartBody.Part?): Observable<Response<Void>>


    @GET("/v2/api/task/fetch/posts/{taskType}/{taskUid}")
    fun getPostsForTask(@Path("taskType") taskType: String,
                        @Path("taskUid") taskUid: String): Observable<List<Post>>


    @GET("/v2/api/gcm/register")
    fun registerGCMToken(@Query("gcmToken") gcmToken: String): Observable<Boolean>

}
