package za.org.grassroot2.view.activity

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_welcome.*
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent


class WelcomeActivity : GrassrootActivity() {



    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_welcome
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (loggedIn()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        createAccBtn.setOnClickListener({
            val next = Intent(this, RegisterActivity::class.java)
            startActivity(next)
            finish()
        })

        signInTxt.setOnClickListener({
            val next = Intent(this, LoginActivity::class.java)
            startActivity(next)
            finish()
        })
    }


}