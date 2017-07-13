package za.org.grassroot.android.services.rest;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by luke on 2017/07/12.
 */

public interface GrassrootRestService {

    @GET("/auth/otp/request")
    Single<String> requestOtp(@Query("msisdn") String msisdn);

}