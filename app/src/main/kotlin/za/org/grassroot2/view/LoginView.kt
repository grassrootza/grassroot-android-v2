package za.org.grassroot2.view

interface LoginView : GrassrootView {

    fun toggleSubmitButton(enabled: Boolean)

    fun loginSuccessContinue(authToken: String, nextActivity: Class<*>)
}