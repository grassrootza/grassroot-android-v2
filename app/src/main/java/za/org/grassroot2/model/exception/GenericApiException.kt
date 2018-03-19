package za.org.grassroot2.model.exception

import okhttp3.ResponseBody

/**
 * Created by luke on 2017/12/18.
 */
class GenericApiException(errorBody: ResponseBody?): RuntimeException()