package za.org.grassroot2.services.rest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import za.org.grassroot2.model.AroundEntity;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.Post;
import za.org.grassroot2.model.TokenResponse;
import za.org.grassroot2.model.alert.LiveWireAlert;
import za.org.grassroot2.model.language.NluResponse;
import za.org.grassroot2.model.request.MemberRequest;
import za.org.grassroot2.model.task.NluParseResult;
import za.org.grassroot2.model.task.NluServerResponse;
import za.org.grassroot2.model.task.PendingResponseDTO;
import za.org.grassroot2.model.task.Task;
import za.org.grassroot2.model.task.Vote;
import za.org.grassroot2.model.task.Todo;

public interface GrassrootUserApi {

    // Fetching groups
    @POST("/v2/api/group/fetch/updated")
    Observable<List<Group>> fetchUserGroups(@Body Map<String, Long> existingUids);

    @GET("/v2/api/group/fetch/info")
    Observable<List<Group>> fetchGroupsInfo(@Query("groupUids") List<String> groupUids);

    @GET("/v2/api/group/fetch/full")
    Observable<Group> fetchFullGroupInfo(@Query("groupUid") String groupUid);

    // Send a media file to the server for storage
    @Multipart
    @POST("/v2/api/media/store/{userUid}")
    Observable<Response<RestResponse<String>>> sendMediaFile(@Path("userUid") String userUid,
                                 @Query("imageKey") String fileUid,
                                 @Query("mediaFunction") String function,
                                 @Query("mimeType") String mimeType,
                                 @Part MultipartBody.Part file);

    // Create a LiveWire alert
    @POST("/v2/api/livewire/create/{userUid}")
    Observable<Response<RestResponse<String>>> createLiveWireAlert(@Path("userUid") String userUid,
                                     @Query("headline") String headline,
                                     @Query("description") String description,
                                     @Query("type") String type,
                                     @Query("groupUid") String groupUid,
                                     @Query("taskUid") String taskUid,
                                     @Query("addLocation") boolean addLocation,
                                     @Query("latitude") double latitude,
                                     @Query("longitude") double longitude,
                                     @Query("mediaFileKeys") Set<String> mediaFileUids);

    @GET("/v2/api/language/parse/intent")
    Observable<NluParseResult> sendNLURequest(@Query("text") String text);

    @GET("/v2/api/task/fetch/todo/responses/{taskUid}")
    Observable<Map<String, String>> fetchTodoResponses(@Path("taskUid") String taskUid);

    @GET("/v2/api/task/fetch/todo/download/{taskUid}")
    Observable<byte[]> downloadTodoResponses(@Path("taskUid") String taskUid);

    @GET("/v2/api/task/respond/todo/information/{todoUid}")
    Observable<Response<Todo>> respondToTodo(@Path("todoUid") String taskUid,
                                             @Query("response") String response );

    @POST("/v2/api/task/fetch/updated/group/{userUid}/{groupUid}")
    Observable<List<Task>> fetchGroupTasksMinimumInfo(@Path("userUid") String userUid, @Path("groupUid") String groupUid, @Body Map<String, Long> timestamps);


    @GET("/v2/api/user/pending")
    Observable<PendingResponseDTO> fetchPendingResponses();

    @POST("/v2/api/task/fetch/specified")
    Observable<List<Task>> fetchTasksByUid(@Body Map<String, String> taskUids);

    @POST("/v2/api/task/fetch/updated")
    Observable<List<Task>> fetchUserTasksMinimumInfo(@Body Map<String, Long> timestamps);

    @POST("/v2/api/group/modify/members/add/{userUid}/{groupUid}")
    Observable<Response<Void>> addMembersToGroup(@Path("userUid") String userId, @Path("groupUid") String groupId, @Body List<MemberRequest> request);

    @POST("/v2/api/group/modify/hide/{groupUid}")
    Observable<Response<Void>> hideGroup(@Path("groupUid") String groupUid);

    @POST("/v2/api/group/modify/leave/{groupUid}")
    Observable<RestResponse<Boolean>> leaveGroup(@Path("groupUid") String groupUid);

    // not bothering to stream since the XLS generated is tiny (< 20kb for large groups)
    @GET("/v2/api/group/fetch/export/{groupUid}")
    Observable<Response<ResponseBody>> fetchGroupMemberSheet(@Path("groupUid") String groupUid);

    @GET("/v2/api/language/parse/datetime/text")
    Observable<Long> getTimestampForTextDate(@Query("text") String text);

    @GET("/v2/api/language/list")
    Observable<Map<String, String>> fetchLanguages();

    @POST("/v2/api/task/create/meeting/{parentType}/{parentUid}")
    Observable<Response<Task>> createMeeting(@Path("parentType") String parentType,
                                          @Path("parentUid") String parentUid,
                                          @Query("subject") String subject,
                                          @Query("location") String location,
                                          @Query("dateTimeEpochMillis") long created,
                                          @Query("description") String description,
                                          @Query("publicMeeting") boolean publicMeeting,
                                          @Query("userLat") double userLatitude,
                                          @Query("userLong") double userLongitude,
                                          @Query("assignedMembersUids") List<String> assignedMembersUids,
                                          @Query("mediaFileUid") String mediaFileUid);

    @POST("/v2/api/task/create/vote/{parentType}/{parentUid}")
    Observable<Response<Task>> createVote(@Path("parentType") String parentType,
                                          @Path("parentUid") String parentUid,
                                          @Query("title") String subject,
                                          @Query("voteOptions") List<String> voteOptions,
                                          @Query("description") String description,
                                          @Query("time") long time,
                                          @Query("mediaFileUid") String mediaFileUid,
                                          @Query("assignedMemberUids") List<String> assignedMemberUids);

    @POST("/v2/api/task/create/todo/action/{parentType}/{parentUid}")
    Observable<Response<Task>> createActionTodo(@Path("parentType") String parentType,
                                                @Path("parentUid") String parentUid,
                                                @Query("subject") String subject,
                                                @Query("dueDateTime") long dueDateTime,
                                                @Query("recurring") boolean recurring,
                                                @Query("recurringPeriodMillis") long recurringPeriodMillis,
                                                @Query("assignedMembersUids") List<String> assignedMembersUids,
                                                @Query("mediaFileUids") List<String> mediaFileUids);

    @POST("/v2/api/task/create/todo/information/{parentType}/{parentUid}")
    Observable<Response<Task>> createInformationTodo(@Path("parentType") String parentType,
                                          @Path("parentUid") String parentUid,
                                          @Query("subject") String subject,
                                          @Query("responseTag") String responseTag,
                                          @Query("dueDateTime") long dueDatetime,
                                          @Query("assignedUids") List<String> assignedUids,
                                          @Query("mediaFileUids") List<String> mediaFileUids);

    @POST("/v2/api/task/create/todo/confirmation/{parentType}/{parentUid}")
    Observable<Response<Task>> createConfirmationTodo(@Path("parentType") String parentType,
                                          @Path("parentUid") String parentUid,
                                          @Query("subject") String subject,
                                          @Query("dueDateTime") long dueDateTime,
                                          @Query("requireImages") boolean requireImages,
                                          @Query("assignedMemberUids") List<String> assignedMemberUids,
                                          @Query("confirmingMemberUids") List<String> confirmingMemberUids,
                                          @Query("recurring") boolean recurring,
                                          @Query("recurringPeriodMillis") long recurringPeriodMillis,
                                          @Query("mediaFileUids") List<String> mediaFileUids);

    @POST("/v2/api/task/create/todo/volunteer/{parentType}/{parentUid}")
    Observable<Response<Task>> createVolunteerTodo(@Path("parentType") String parentType,
                                          @Path("parentUid") String parentUid,
                                          @Query("subject") String subject,
                                          @Query("dueDateTime") long dueDateTime,
                                          @Query("assignedMemberUids") List<String> assignedMemberUids,
                                          @Query("mediaFileUids") List<String> mediaFileUids);

    @POST("/v2/api/group/modify/create")
    Observable<Response<Group>> createGroup(@Query("name") String groupName,
                                           @Query("description") String description,
                                           @Query("permissionTemplate") String permissionTemplate,
                                           @Query("reminderMinutes") int reminderMinutes,
                                           @Query("discoverable") boolean discoverable,
                                           @Query("defaultAddToAccount") boolean defaultAddToAccount,
                                           @Query("pinGroup") boolean pinGroup);

    @GET("/v2/api/location/all/alerts/{userUid}")
    Observable<List<LiveWireAlert>> getAlertsAround(@Path("userUid") String userUid,
                                                @Query("longitude") double longitude,
                                                @Query("latitude") double latitude,
                                                @Query("radiusMetres") int radius);

    @GET("/v2/api/location/all/{userUid}")
    Observable<List<AroundEntity>> getAllAround(@Path("userUid") String userUid,
                                                @Query("longitude") double longitude,
                                                @Query("latitude") double latitude,
                                                @Query("radiusMetres") int radius,
                                                @Query("saerchType") String serachType);


    @Multipart
    @POST("/v2/api/user/profile/image/change")
    Observable<Response<RestResponse<String>>> uploadProfilePhoto(@Part MultipartBody.Part file);


    @POST("/v2/api/user/profile/data/update")
    Observable<RestResponse<TokenResponse>> updateProfileData(
            @Query("displayName") String displayName,
            @Query("phoneNumber") String phoneNumber,
            @Query("email") String email,
            @Query("languageCode") String languageCode);


    @POST("/v2/api/task/respond/meeting/{userUid}/{taskUid}")
    Observable<Response<Void>> respondToMeeting(@Path("userUid") String userId, @Path("taskUid") String taskUid, @Query("response") String response);

    @POST("/v2/api/task/respond/vote/{taskUid}")
    Observable<Response<Vote>> respondToVote(@Path("taskUid") String taskUid, @Query("vote") String vote);

    @Multipart
    @POST("/v2/api/task/respond/post/{userUid}/{taskType}/{taskUid}")
    Observable<Response<Void>> uploadPost(@Path("userUid") String userId,
                                          @Path("taskType") String taskType,
                                          @Path("taskUid") String taskUid,
                                          @Query("caption") String title,
                                          @Part MultipartBody.Part file);

    @GET("/v2/api/language/parse/intent")
    Flowable<NluResponse> seekIntentInText(@Query("text") String text);

    @Multipart
    @POST("/v2/api/language/parse/speech")
    Observable<Response<Void>> uploadSpeech(@Query("sampleRate") Integer sampleRate,
                                          @Query("parseForIntent") boolean parseForIntent,
                                          @Part MultipartBody.Part file);

    @GET("/v2/api/task/fetch/posts/{userUid}/{taskType}/{taskUid}")
    Observable<List<Post>> getPostsForTask(@Path("userUid") String userId,
                                           @Path("taskType") String taskType,
                                           @Path("taskUid") String taskUid);


    @GET("/v2/api/gcm/register")
    Observable<Boolean> registerGCMToken(@Query("gcmToken") String gcmToken);

}
