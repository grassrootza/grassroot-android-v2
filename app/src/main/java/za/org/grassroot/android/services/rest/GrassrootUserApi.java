package za.org.grassroot.android.services.rest;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import za.org.grassroot.android.model.Group;
import za.org.grassroot.android.model.TokenResponse;

public interface GrassrootUserApi {

    // Fetching groups
    @GET("/api/group/get/all")
    Single<List<Group>> fetchAllGroups(@Query("userUid") String userUid);
}
