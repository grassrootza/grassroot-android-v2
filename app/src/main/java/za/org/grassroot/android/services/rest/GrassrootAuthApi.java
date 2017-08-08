package za.org.grassroot.android.services.rest;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import za.org.grassroot.android.model.TokenResponse;

/**
 * Created by luke on 2017/08/08.
 */

public interface GrassrootAuthApi {

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

}
