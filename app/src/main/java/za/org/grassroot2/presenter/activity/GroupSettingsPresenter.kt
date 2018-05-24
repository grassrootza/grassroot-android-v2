package za.org.grassroot2.presenter.activity

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.FileProvider
import android.view.View
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Group
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.util.FileUtil
import za.org.grassroot2.view.GrassrootView
import za.org.grassroot2.view.activity.DashboardActivity
import za.org.grassroot2.view.activity.MembersActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by luke on 2017/12/03.
 */
class GroupSettingsPresenter @Inject
constructor(private val networkService: NetworkService, private val dbService: DatabaseService) : BasePresenter<GroupSettingsPresenter.GroupSettingsView>() {

    private var groupUid: String? = null
    private var group: Group? = null

    fun init(groupUid: String) {
        this.groupUid = groupUid;
    }

    fun loadData() {
        disposableOnDetach(dbService.load(Group::class.java, groupUid!!).subscribeOn(io()).observeOn(main()).subscribe({ g ->
            group = g
            view.render(g, countMembersLastMonth(g))
            fetchFullGroupBackground(groupUid!!)
        }, { it.printStackTrace() }))
    }

    private fun countMembersLastMonth(g: Group) : Long {
        val monthAgoMillis = Date().time - 30L*24*60*60*1000
        return dbService.countMembersJoinedSince(g.uid, monthAgoMillis)
    }

    private fun fetchFullGroupBackground(groupUid: String) {
        networkService.downloadCompleteGroupInfo(groupUid).subscribeOn(io()).observeOn(main())
                .subscribe ({  g ->
                    Timber.d("Success! fetched group .. ready for member fragment")
                    view.membersAvailable(g.memberCount, countMembersLastMonth(g))
                }, { it.printStackTrace() })
    }

    fun hideGroup() {
        Timber.d("Showing progress bar 1 in GroupSettingsPresenter")
        view.showProgressBar()
        group?.isHidden = true
        dbService.store(Group::class.java, group!!)
                .flatMapObservable { group -> networkService.hideGroup(group) }.subscribeOn(io()).observeOn(main())
                .subscribe({ b ->
                    view.closeProgressBar()
                    when (b) {
                        true -> view.showSuccessDialog(R.string.group_hide_done,
                                View.OnClickListener { view.launchActivity(DashboardActivity::class.java, Bundle()) })
                        else -> view.showErrorDialog(R.string.group_hide_error)
                    }
                }, { t ->
                    // todo: if network call fails, store it in sync and send it later
                    view.closeProgressBar()
                    handleNetworkUploadError(t)
                })
    }

    fun leaveGroup() {
        Timber.d("Showing progress bar 2 in GroupSettingsPresenter")
        view.showProgressBar()
        networkService.leaveGroup(group!!).subscribeOn(io()).observeOn(main())
                .subscribe({ b ->
                    view.closeProgressBar()
                    when (b) {
                        true -> view.showSuccessDialog(R.string.group_left_done,
                                View.OnClickListener { view.launchActivity(DashboardActivity::class.java, Bundle()) })
                        else -> view.showErrorDialog(R.string.group_leaving_error)
                    }
                }, {t ->
                    view.closeProgressBar()
                    handleNetworkUploadError(t) // todo: check whether to still catch / doOnResume inside network service
                })
    }

    fun exportMembers() {
        Timber.e("starting the calls")
        view.ensureWriteExteralStoragePermission()
                .filter { aBoolean -> aBoolean }
                .subscribeOn(io())
                .flatMap { _ ->
                    Timber.e("on main thread still?")
                    networkService.downloadMemberFile(group!!)
                }
                .flatMapSingle { data -> writeMembersToFile(data) }
                .subscribeOn(io()).observeOn(main()).subscribe({ uri ->
            Timber.d("saved the file!")
            view.selectFileActionDialog(uri)
        }, { Timber.e(it) })
    }

    fun loadMembers() {
        val b = Bundle()
        b.putString(MembersActivity.GROUP_UID_FIELD, groupUid)
        view.launchActivity(MembersActivity::class.java, b)
    }

    private fun writeMembersToFile(data: ByteArray): Single<Uri> {
        return Single.create { e->
            val xlsFile = createXlsFile()
            Timber.d("XLS file created, here is path: ${xlsFile.absolutePath}")
            xlsFile.writeBytes(data)
            val xlsUri = FileProvider.getUriForFile(view.activity.applicationContext,
                    FileUtil.FILEPROVIDER_AUTHORITY,
                    xlsFile)
            Timber.d("storing XLS in file, URI = $xlsUri")
            e.onSuccess(xlsUri)
        }
    }

    @Throws(IOException::class)
    private fun createXlsFile(): File {
        val prefix = group!!.name.toLowerCase().trim().replace(" ", "-")
        val timeStamp = GRP_XLS_TIMESTAMP.format(Date())
        val storageDirectory = view.activity.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (!storageDirectory!!.exists() && !storageDirectory.mkdirs()) {
            Timber.e("Could not create download folder")
        }
        return File.createTempFile(
                prefix + "_" + timeStamp + "_",
                ".xlsx",
                storageDirectory
        )
    }

    interface GroupSettingsView : GrassrootView {
        fun render(group: Group, lastMonthCount: Long)
        fun membersAvailable(totalMembers: Int, lastMonthCount: Long)
        fun exitToHome(messageToUser: Int)
        fun ensureWriteExteralStoragePermission(): Observable<Boolean>
        fun selectFileActionDialog(fileUri: Uri)
    }

    companion object {
        private val GRP_XLS_TIMESTAMP = SimpleDateFormat("yyyyMMdd_HHmmss")
    }

}