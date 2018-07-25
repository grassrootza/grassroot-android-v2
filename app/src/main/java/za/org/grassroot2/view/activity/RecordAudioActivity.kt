package za.org.grassroot2.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_record_audio.*
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.presenter.activity.RecordAudioPresenter
import java.io.File
import javax.inject.Inject

class RecordAudioActivity : GrassrootActivity(), RecordAudioPresenter.RecordAudioView {

    @Inject lateinit var rxPermission : RxPermissions
    @Inject lateinit var presenter: RecordAudioPresenter

    override val layoutResourceId: Int
        get(): Int = R.layout.activity_record_audio

    override fun onInject(component: ActivityComponent) = component.inject(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.attach(this)
        okButton.setOnClickListener {
            val audioFileName = presenter.completeRecording()
            audioFileName?.let {
                val intent = Intent()
                intent.data = Uri.fromFile(File(audioFileName))
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
        disposables.addAll(rxPermission.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO).subscribe({result ->
            if (result) {
                presenter.startRecording()
            } else {
                finish()
            }
        }, {t -> Timber.e(t) }))
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    override fun startAnimation() {
        iconImageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse_animation))
    }

    override fun stopAnimation() {
        iconImageView.clearAnimation()
    }

    companion object {
        fun start(context: Activity, requestCode: Int) {
            context.startActivityForResult(Intent(context, RecordAudioActivity::class.java), requestCode)
        }
    }

}
