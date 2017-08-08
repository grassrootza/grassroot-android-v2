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

public interface GrassrootRestService {

    // Some authentication related methods
    @GET("/api/user/login/{msisdn}")
    Observable<RestResponse<String>> requestOtp(@Path("msisdn") String msisdn);

    @GET("/api/auth/login")
    Observable<RestResponse<TokenResponse>> validateOtp(@Query("phoneNumber") String msisdn,
                                                        @Query("otp") String otp,
                                                        @Query("clientType") String clientType);

    @GET("/api/auth/refresh")
    Call<RestResponse<TokenResponse>> refreshOtp(@Query("phoneNumber") String msisdn,
                                                 @Query("clientType") String clientType);

    @GET("/api/auth/token/validate")
    Single<RestResponse> validateToken(@Query("token") String token);


    // Fetching groups
    @GET("/api/group/get/all")
    Single<List<Group>> fetchAllGroups(@Query("userUid") String userUid);
}
