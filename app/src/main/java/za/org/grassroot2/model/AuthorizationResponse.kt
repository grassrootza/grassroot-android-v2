package za.org.grassroot2.model

/**
 * Created by luke on 2018/01/18.
 */
data class AuthorizationResponse(val errorCode: String, val user: TokenResponse) {
}