package za.org.grassroot2.model

class AuthenticatedUser(
        val userUid: String,
        val msisdn: String,
        val displayName: String,
        val email: String,
        val languageCode: String,
        val systemRoleName: String,
        val token: String
)
