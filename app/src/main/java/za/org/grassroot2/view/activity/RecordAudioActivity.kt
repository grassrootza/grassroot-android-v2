package za.org.grassroot2.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_create_action.*
import kotlinx.android.synthetic.main.activity_record_audio.*
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.dagger.activity.ActivityComponent
import za.org.grassroot2.presenter.activity.RecordAudioPresenter
import za.org.grassroot2.rxbinding.RxTextView
import za.org.grassroot2.rxbinding.RxView
import za.org.grassroot2.view.adapter.GenericViewPagerAdapter
import za.org.grassroot2.view.fragment.ActionSingleInputFragment
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RecordAudioActivity : GrassrootActivity(), RecordAudioPresenter.RecordAudioView {

    @Inject lateinit var rxPermission : RxPermissions
    @Inject lateinit var presenter: RecordAudioPresenter
    private var multiLine: Boolean = false

    override fun onInject(component: ActivityComponent) {
        component.inject(this)
    }

    override fun getLayoutResourceId(): Int = R.layout.activity_record_audio


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.attach(this)
        // setMultilineIfRequired()
        okButton.setOnClickListener {
            val mortalRequest = nlu_input_container.getEditText()?.getText().toString()
            if (mortalRequest == "alpha") {
                val request = "I would like to set a meeting"
                presenter.thisIsWhatTheHumanWants(request)
            }
            else if (mortalRequest == "bravo") {
                val request = "set a meeting tomorrow at 12pm"
                presenter.thisIsWhatTheHumanWants(request)
            }
            else if (mortalRequest == "charlie") {
                val request = "set a meeting tomorrow at 12pm in Braamfontein"
                presenter.thisIsWhatTheHumanWants(request)
            }
            else {
                presenter.thisIsWhatTheHumanWants(mortalRequest)
            }
        }
        /*presenter.attach(this)
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
        */
    }

    private fun setMultilineIfRequired() {
        if (multiLine) {
            nlu_input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE)
            nlu_input.setSingleLine(false)
            nlu_input.setGravity(Gravity.LEFT or Gravity.TOP)
            nlu_input.setMaxLines(5)
            nlu_input.setMinLines(5)
        }
    }

    override fun initiateCreateAction(actionToInitiate: Int) {
        Timber.d("initiating create action activity, with actionToInitiate ... " + actionToInitiate)
        CreateActionActivity.startOnAction(activity, actionToInitiate, null)
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

    fun setMultiLine(multiLine: Boolean) {
        this.multiLine = multiLine
    }

    fun isMultiLine(): Boolean {
        return multiLine
    }
}

