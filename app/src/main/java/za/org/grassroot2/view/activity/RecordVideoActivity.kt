package za.org.grassroot2.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_record_audio.*
import kotlinx.android.synthetic.main.activity_record_video.*
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.presenter.activity.RecordAudioPresenter
import za.org.grassroot2.presenter.activity.RecordVideoPresenter
import java.io.File
import javax.inject.Inject

class RecordVideoActivity : GrassrootActivity(), RecordVideoPresenter.RecordVideoView {

    @Inject lateinit var rxPermission : RxPermissions
    @Inject lateinit var presenter: RecordVideoPresenter

    override val layoutResourceId: Int
        get(): Int = R.layout.activity_record_video

    override fun onInject(component: ActivityComponent) = component.inject(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.attach(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    override fun startAnimation() {
    }

    override fun stopAnimation() {
    }

    companion object {
        fun start(context: Activity, requestCode: Int) {
            context.startActivityForResult(Intent(context, RecordVideoActivity::class.java), requestCode)
        }
    }

}
