package za.org.grassroot2.view.activity

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_welcome.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.presenter.WelcomePresenter
import za.org.grassroot2.view.WelcomeView
import javax.inject.Inject


class WelcomeActivity : GrassrootActivity(), WelcomeView {


    @Inject
    lateinit var presenter: WelcomePresenter

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_welcome
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        presenter.attach(this)

        if (loggedIn()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        createAccBtn.setOnClickListener({
            presenter.navigateToCreateAccountScreen()
        })

        signInTxt.setOnClickListener({
            presenter.navigateToLoginScreen()
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }
}