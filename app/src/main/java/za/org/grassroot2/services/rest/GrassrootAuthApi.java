package za.org.grassroot2.services.rest;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import za.org.grassroot2.model.AuthorizationResponse;
import za.org.grassroot2.model.TokenResponse;

/**
 * Created by luke on 2017/08/08.
 */

public interface GrassrootAuthApi {

    // Some authentication related methods
    @GET("/v2/api/auth/token/refresh")
    Observable<Response<RestResponse<String>>> refreshOtp(@Query("oldToken") String oldToken,
                                                          @Query("durationMillis") Long expiresIn);

    @GET("/v2/api/auth/token/validate")
    Single<RestResponse> validateToken(@Query("token") String token);

    @POST("/v2/api/auth/login-password")
    Observable<Response<AuthorizationResponse>> login(@Query("username") String msisdn,
                                                      @Query("password") String password,
                                                      @Query("interfaceType") String interfaceType);


    @POST("/v2/api/auth/register")
    Observable<RestResponse<String>> register(@Query("phoneNumber") String phoneNumber,
                                              @Query("displayName") String displayName,
                                              @Query("password") String password);

    @GET("/v2/api/auth/register/verify/{phoneNumber}/{code}")
    Observable<RestResponse<TokenResponse>> verifyRegistrationCode(@Path("phoneNumber") String phoneNumber, @Path("code") String code);

    @GET("/v2/api/auth/reset-password-request")
    Observable<RestResponse<String>> resetPasswordRequest(@Query("phoneNumber") String phoneNumber);

    @GET("/v2/api/auth/reset-password-confirm")
    Observable<RestResponse<String>> resetPasswordConfirm(@Query("phoneNumber") String phoneNumber,
                                                          @Query("password") String newPassword,
                                                          @Query("code") String otpCode);


}
