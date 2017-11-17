package za.org.grassroot2.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.support.annotation.RequiresApi
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class MediaRecorderWrapper @Inject constructor(val context: Context) {

    private var mediaRecorder: MediaRecorder? = null

    private lateinit var filename: String

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    fun initAndStartRecording() {
        mediaRecorder = MediaRecorder()
        mediaRecorder?.let {
            it.setAudioSource(MediaRecorder.AudioSource.MIC)
            it.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB)
            it.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
            it.setAudioSamplingRate(SAMPLING_RATE)
            filename = context.externalCacheDir.absolutePath + File.separator + System.currentTimeMillis().toString()
            it.setOutputFile(filename)
            try {
                it.prepare()
                it.start()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun cancelRecording() {
        mediaRecorder?.let {
            it.stop()
            it.release()
            File(filename).delete()
            mediaRecorder = null
        }
    }

    fun stopRecoring() : String? {
        mediaRecorder?.let {
            it.stop()
            it.release()
            mediaRecorder = null
            return filename
        }
        return null
    }

    companion object {
        val SAMPLING_RATE = 16000
    }
}