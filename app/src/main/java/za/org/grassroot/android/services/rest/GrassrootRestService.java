package za.org.grassroot.android.services.rest;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import za.org.grassroot.android.model.TokenResponse;

/**
 * Created by luke on 2017/07/12.
 */

public interface GrassrootRestService {

    @GET("/api/user/login/{msisdn}")
    Observable<RestResponse<String>> requestOtp(@Path("msisdn") String msisdn);

    @GET("/api/auth/login")
    Observable<RestResponse<TokenResponse>> validateOtp(@Query("phoneNumber") String msisdn,
                                                        @Query("otp") String otp,
                                                        @Query("clientType") String clientType);

}