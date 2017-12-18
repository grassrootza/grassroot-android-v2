package za.org.grassroot2.services.rest

import com.google.gson.JsonParser
import okhttp3.ResponseBody
import timber.log.Timber

/**
 * Created by luke on 2017/12/18.
 */
class ApiError constructor(errorBody: ResponseBody?) {

    var errorCode = "GENERIC_ERROR"

    init {
        if (errorBody != null) {
            val errorJsonString = errorBody.string()
            Timber.e("error body received as: %s", errorJsonString)
            this.errorCode = JsonParser().parse(errorJsonString)
                    .asJsonObject["message"].asString
        }
    }

}