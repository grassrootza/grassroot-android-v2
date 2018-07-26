package za.org.grassroot2.presenter.activity

import android.net.Uri
import org.greenrobot.eventbus.EventBus

import java.util.Collections

import javax.inject.Inject

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import za.org.grassroot2.R
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.Group
import za.org.grassroot2.model.MediaFile
import za.org.grassroot2.model.RequestMapper
import za.org.grassroot2.model.contact.Contact
import za.org.grassroot2.model.util.GroupPermissionChecker
import za.org.grassroot2.services.MediaService
import za.org.grassroot2.services.NetworkService
import za.org.grassroot2.services.rest.GrassrootUserApi
import za.org.grassroot2.view.GrassrootView
import java.io.File


class GroupDetailsPresenter @Inject
constructor(private val databaseService: DatabaseService,
            private val networkService: NetworkService,
            private val mediaService: MediaService,
            private val grassrootUserApi: GrassrootUserApi) : BasePresenter<GroupDetailsPresenter.GroupDetailsView>() {

    private var groupUid: String? = null
    private var currentMediaFileUid: String? = null

    fun init(groupUid: String) {
        this.groupUid = groupUid
    }

    fun loadData() {
        disposableOnDetach(databaseService.load(Group::class.java, groupUid!!).subscribeOn(io()).observeOn(main()).subscribe({ group ->
            if (view != null) {
                Timber.d("in GroupDetailsPresenter. View is not null.")
                if (GroupPermissionChecker.hasCreatePermission(group)) {
                    view.displayFab()
                    Timber.d("User has create groups permissions. Proceed to the get down.")
                }
                view.render(group)
            }
        }, { it.printStackTrace() }))
        disposableOnDetach(networkService.getTasksForGroup(groupUid!!).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({ tasks ->
            if (!tasks.isEmpty()) {
                databaseService.storeTasks(tasks)
                EventBus.getDefault().post(TasksUpdatedEvent())
            } else {
                view.emptyData()
            }
        }, { this.handleNetworkConnectionError(it) }))

    }

    fun inviteContacts(contacts: List<Contact>?) {
        view.showProgressBar()
        disposableOnDetach(networkService.inviteContactsToGroup(groupUid!!, RequestMapper.map(groupUid, contacts)).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe({ voidResponse ->
            view.closeProgressBar()
            if (voidResponse.isSuccessful) {
            } else {
                view.showErrorSnackbar(R.string.error_permission_denied)
            }
        }, { this.handleNetworkUploadError(it) }))
    }

    fun inviteContact(name: String, phone: String) {
        val c = Contact()
        c.displayName = name
        c.setPhoneNumber(phone)
        inviteContacts(listOf(c))
    }

    fun takePhoto() {
        disposableOnDetach(
                mediaService
                        .createFileForMedia("image/jpeg", MediaFile.FUNCTION_GROUP_PROFILE_PHOTO)
                        .subscribeOn(io())
                        .observeOn(main())
                        .subscribe({ s ->
                            val mediaFile = databaseService.loadObjectByUid(MediaFile::class.java, s)
                            Timber.e("mediaFile stored and retrieved, = " + mediaFile!!)
                            // for some reason, sometimes it comes back null ...
                            Timber.d("media URI passed to intent: " + Uri.parse(mediaFile.contentProviderPath))
                            currentMediaFileUid = s

                            view.cameraForResult(mediaFile.contentProviderPath, s)
                        }) { throwable ->
                            Timber.e(throwable, "Error creating file")
                            view.showErrorSnackbar(R.string.error_file_creation)
                        })
    }

    private fun uploadProfilePhoto(mediaFile: MediaFile){
        val fileMultipart = getFileMultipart(mediaFile, "image")
        Timber.d("E kaba ra fihla mo nang?????????")

        disposableOnDetach(
                grassrootUserApi.uploadGroupProfilePhoto(groupUid,fileMultipart)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { result ->
                                    Timber.d("What am i getting here @@@@@ ----->>>>>  %S",result)
                                    val group = databaseService.loadGroup(groupUid as String)

                                    Timber.d("Group profileImageUrl---------------------------->>>>>>>>>>>>>>>>>>>>>> %s",group?.profileImageUrl)
                                },
                                { error ->
                                    //view.showErrorDialog(R.string.me_error_updating_photo)
                                    Timber.d("It gets here_______________>>>>>>>>>>>>>>>>>>>>>")
                                    Timber.e(error)
                                }
                        )
        )
    }

    private fun getFileMultipart(mediaFile: MediaFile, paramName: String): MultipartBody.Part? {
        return try {
            val file = File(mediaFile.absolutePath)
            val requestFile = RequestBody.create(MediaType.parse(mediaFile.mimeType), file)
            MultipartBody.Part.createFormData(paramName, file.name, requestFile)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    fun cameraResult(){
        disposableOnDetach(mediaService.captureMediaFile(currentMediaFileUid!!,500,500)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            val mediaFile = databaseService.loadObjectByUid(MediaFile::class.java, currentMediaFileUid!!)
                            if (mediaFile != null)
                                uploadProfilePhoto(mediaFile)
                        },
                        {
                            Timber.d(it)
                        }
                )
        )
    }

    interface GroupDetailsView : GrassrootView {
        fun render(group: Group)
        fun emptyData()
        fun displayFab()
        fun cameraForResult(contentProviderPath: String, s: String)
        fun setImage(imageUrl:String)
    }

    class TasksUpdatedEvent
}
