package za.org.grassroot.android.services.rest;

import java.util.List;
import java.util.Set;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import za.org.grassroot.android.model.Group;

public interface GrassrootUserApi {

    // Fetching groups
    @GET("/api/group/get/all")
    Single<List<Group>> fetchAllGroups(@Query("userUid") String userUid);

    // Send a media file to the server for storage
    @Multipart
    @POST("/api/mobile/media")
    Call<RestResponse<String>> sendMediaFile(@Query("userUid") String userUid,
                                 @Query("imageKey") String fileUid,
                                 @Query("mediaFunction") String function,
                                 @Part("file") MultipartBody.Part file);

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
