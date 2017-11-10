package za.org.grassroot2.view

interface ForgottenPasswordView : GrassrootView {


    fun passwordChangeSuccess(password: String, nextActivity: Class<*>)


}