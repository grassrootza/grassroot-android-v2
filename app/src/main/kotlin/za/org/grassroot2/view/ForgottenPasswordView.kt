package za.org.grassroot2.view

interface ForgottenPasswordView : GrassrootView {


    fun switchToPhoneNumberInput()
    fun switchToPasswordInput()
    fun switchToOtpInput(otpValue: String)
    fun passwordChangeSuccess(password: String)


}