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
import za.org.grassroot2.model.task.Task;
import za.org.grassroot2.model.task.Vote;

public interface GrassrootUserApi {

    // Fetching groups
    @POST("/api/group/fetch/updated/{userUid}")
    Observable<List<Group>> fetchUserGroups(@Path("userUid") String userUid,
                                                          @Body Map<String, Long> existingUids);

    @GET("/api/group/fetch/info/{userUid}")
    Observable<List<Group>> fetchGroupsInfo(@Path("userUid") String userUid,
                                                          @Query("groupUids") List<String> groupUids);

    @GET("/api/group/fetch/full")
    Observable<Group> fetchFullGroupInfo(@Query("groupUid") String groupUid);

    // Send a media file to the server for storage
    @Multipart
    @POST("/api/media/store/{userUid}")
    Observable<Response<RestResponse<String>>> sendMediaFile(@Path("userUid") String userUid,
                                 @Query("imageKey") String fileUid,
                                 @Query("mediaFunction") String function,
                                 @Query("mimeType") String mimeType,
                                 @Part MultipartBody.Part file);

    // Create a LiveWire alert
    @POST("/api/livewire/create/{userUid}")
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

    @POST("/api/task/fetch/updated/group/{userUid}/{groupUid}")
    Observable<List<Task>> fetchGroupTasksMinimumInfo(@Path("userUid") String userUid, @Path("groupUid") String groupUid, @Body Map<String, Long> timestamps);

    @POST("/api/task/fetch/specified/{userUid}")
    Observable<List<Task>> fetchTasksByUid(@Path("userUid") String userUid, @Body Map<String, String> taskUids);

    @POST("/api/task/fetch/updated/{userUid}")
    Observable<List<Task>> fetchUserTasksMinimumInfo(@Path("userUid") String userUid, @Body Map<String, Long> timestamps);

    @POST("/api/group/modify/members/add/{userUid}/{groupUid}")
    Observable<Response<Void>> addMembersToGroup(@Path("userUid") String userId, @Path("groupUid") String groupId, @Body List<MemberRequest> request);

    @POST("/api/group/modify/hide/{groupUid}")
    Observable<Response<Void>> hideGroup(@Path("groupUid") String groupUid);

    @POST("/api/group/modify/leave/{groupUid}")
    Observable<RestResponse<Boolean>> leaveGroup(@Path("groupUid") String groupUid);

    // not bothering to stream since the XLS generated is tiny (< 20kb for large groups)
    @GET("/api/group/fetch/export/{groupUid}")
    Observable<Response<ResponseBody>> fetchGroupMemberSheet(@Path("groupUid") String groupUid);

    @GET("/api/language/parse/datetime/text")
    Observable<Long> getTimestampForTextDate(@Query("text") String text);

    @GET("/api/language/list")
    Observable<Map<String, String>> fetchLanguages();

    @POST("/api/task/create/meeting/{userUid}/{parentType}/{parentUid}")
    Observable<Response<Task>> createTask(@Path("parentType") String parentType,
                                          @Path("userUid") String userUid,
                                          @Path("parentUid") String parentUid,
                                          @Query("subject") String subject,
                                          @Query("location") String location,
                                          @Query("dateTimeEpochMillis") long created);

    @GET("/api/location/all/alerts/{userUid}")
    Observable<List<LiveWireAlert>> getAlertsAround(@Path("userUid") String userUid,
                                                @Query("longitude") double longitude,
                                                @Query("latitude") double latitude,
                                                @Query("radiusMetres") int radius);

    @GET("/api/location/all/{userUid}")
    Observable<List<AroundEntity>> getAllAround(@Path("userUid") String userUid,
                                                @Query("longitude") double longitude,
                                                @Query("latitude") double latitude,
                                                @Query("radiusMetres") int radius,
                                                @Query("saerchType") String serachType);


    @Multipart
    @POST("/api/user/profile/image/change")
    Observable<Response<RestResponse<String>>> uploadProfilePhoto(@Part MultipartBody.Part file);


    @POST("/api/user/profile/data/update")
    Observable<RestResponse<TokenResponse>> updateProfileData(
            @Query("displayName") String displayName,
            @Query("phoneNumber") String phoneNumber,
            @Query("email") String email,
            @Query("languageCode") String languageCode);


    @POST("/api/task/respond/meeting/{userUid}/{taskUid}")
    Observable<Response<Void>> respondToMeeting(@Path("userUid") String userId, @Path("taskUid") String taskUid, @Query("response") String response);

    @POST("/api/task/respond/vote/{taskUid}")
    Observable<Response<Vote>> respondToVote(@Path("taskUid") String taskUid, @Query("vote") String vote);

    @Multipart
    @POST("/api/task/respond/post/{userUid}/{taskType}/{taskUid}")
    Observable<Response<Void>> uploadPost(@Path("userUid") String userId,
                                          @Path("taskType") String taskType,
                                          @Path("taskUid") String taskUid,
                                          @Query("caption") String title,
                                          @Part MultipartBody.Part file);

    @GET("/api/language/parse/intent")
    Flowable<NluResponse> seekIntentInText(@Query("text") String text);

    @Multipart
    @POST("/api/language/parse/speech")
    Observable<Response<Void>> uploadSpeech(@Query("sampleRate") Integer sampleRate,
                                          @Query("parseForIntent") boolean parseForIntent,
                                          @Part MultipartBody.Part file);

    @GET("/api/task/fetch/posts/{userUid}/{taskType}/{taskUid}")
    Observable<List<Post>> getPostsForTask(@Path("userUid") String userId,
                                           @Path("taskType") String taskType,
                                           @Path("taskUid") String taskUid);


    @GET("/api/gcm/register")
    Observable<Boolean> registerGCMToken(@Query("gcmToken") String gcmToken);

}
