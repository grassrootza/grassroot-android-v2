package za.org.grassroot2.view


interface RegistrationView : GrassrootView {

    fun switchToUserNameInput()
    fun switchToPhoneNumberInput()
    fun switchToPasswordInput()
    fun switchToOtpInput(otpValue: String)
    fun switchToSuccessScreen(authToken: String)
    fun finishRegistration(authToken: String, nextActivity: Class<*>)

}