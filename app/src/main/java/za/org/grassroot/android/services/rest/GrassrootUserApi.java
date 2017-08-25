package za.org.grassroot.android.services.rest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import za.org.grassroot.android.model.Group;

public interface GrassrootUserApi {

    // Fetching groups
    @POST("/api/mobile/group/fetch/updated/{userUid}")
    Observable<RestResponse<List<Group>>> fetchUserGroups(@Path("userUid") String userUid,
                                                          @Body Map<String, Long> existingUids);

    @GET("/api/mobile/group/fetch/info/{userUid}")
    Observable<RestResponse<List<Group>>> fetchGroupsInfo(@Path("userUid") String userUid,
                                                          @Query("groupUids") List<String> groupUids);

    // Send a media file to the server for storage
    @Multipart
    @POST("/api/mobile/media/store/{userUid}")
    Call<RestResponse<String>> sendMediaFile(@Path("userUid") String userUid,
                                 @Query("imageKey") String fileUid,
                                 @Query("mediaFunction") String function,
                                 @Query("mimeType") String mimeType,
                                 @Part MultipartBody.Part file);

    // Create a LiveWire alert
    @POST("/api/mobile/livewire")
    Call<RestResponse<String>> createLiveWireAlert(@Query("userUid") String userUid,
                                     @Query("headline") String headline,
                                     @Query("description") String description,
                                     @Query("type") String type,
                                     @Query("groupUid") String groupUid,
                                     @Query("taskUid") String taskUid,
                                     @Query("addLocation") boolean addLocation,
                                     @Query("latitude") double latitude,
                                     @Query("longitude") double longitude,
                                     @Query("mediaFileKeys") Set<String> mediaFileUids);



}
