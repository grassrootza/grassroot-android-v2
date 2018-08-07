package za.org.grassroot2.services.rest

import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import za.org.grassroot2.model.AuthorizationResponse
import za.org.grassroot2.model.TokenResponse

/**
 * Created by luke on 2017/08/08.
 */

interface GrassrootAuthApi {

    // Some authentication related methods
    @GET("/v2/api/auth/token/refresh")
    fun refreshOtp(@Query("oldToken") oldToken: String,
                   @Query("durationMillis") expiresIn: Long?): Observable<Response<RestResponse<String>>>

    @GET("/v2/api/auth/token/validate")
    fun validateToken(@Query("token") token: String): Single<RestResponse<*>>

    @POST("/v2/api/auth/login-password")
    fun login(@Query("username") msisdn: String,
              @Query("password") password: String,
              @Query("interfaceType") interfaceType: String): Observable<Response<AuthorizationResponse>>


    @POST("/v2/api/auth/register")
    fun register(@Query("phoneNumber") phoneNumber: String,
                 @Query("displayName") displayName: String,
                 @Query("password") password: String): Observable<RestResponse<String>>

    @GET("/v2/api/auth/register/verify/{phoneNumber}/{code}")
    fun verifyRegistrationCode(@Path("phoneNumber") phoneNumber: String, @Path("code") code: String): Observable<RestResponse<TokenResponse>>

    @GET("/v2/api/auth/reset-password-request")
    fun resetPasswordRequest(@Query("phoneNumber") phoneNumber: String): Observable<RestResponse<String>>

    @GET("/v2/api/auth/reset-password-confirm")
    fun resetPasswordConfirm(@Query("phoneNumber") phoneNumber: String,
                             @Query("password") newPassword: String,
                             @Query("code") otpCode: String): Observable<RestResponse<String>>


}
