package za.org.grassroot2.view

interface Login2View : GrassrootView {

    fun toggleSubmitButton(enabled: Boolean)

    fun loginSuccessContinue(authToken: String, nextActivity: Class<*>)
}