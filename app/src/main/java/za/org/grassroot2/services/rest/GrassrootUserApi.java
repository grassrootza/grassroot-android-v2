package za.org.grassroot2.services.rest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import za.org.grassroot2.model.Group;
import za.org.grassroot2.model.request.MemberRequestObject;
import za.org.grassroot2.model.task.Task;

public interface GrassrootUserApi {

    // Fetching groups
    @POST("/api/group/fetch/updated/{userUid}")
    Observable<List<Group>> fetchUserGroups(@Path("userUid") String userUid,
                                                          @Body Map<String, Long> existingUids);

    @GET("/api/group/fetch/info/{userUid}")
    Observable<List<Group>> fetchGroupsInfo(@Path("userUid") String userUid,
                                                          @Query("groupUids") List<String> groupUids);

    // Send a media file to the server for storage
    @Multipart
    @POST("/api/media/store/{userUid}")
    Call<RestResponse<String>> sendMediaFile(@Path("userUid") String userUid,
                                 @Query("imageKey") String fileUid,
                                 @Query("mediaFunction") String function,
                                 @Query("mimeType") String mimeType,
                                 @Part MultipartBody.Part file);

    // Create a LiveWire alert
    @POST("/api/livewire/create/{userUid}")
    Call<RestResponse<String>> createLiveWireAlert(@Path("userUid") String userUid,
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
    Observable<Response<Void>> addMembersToGroup(@Path("userUid") String userId, @Path("groupUid") String groupId, @Body List<MemberRequestObject> request);

}
