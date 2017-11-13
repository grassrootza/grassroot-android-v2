package za.org.grassroot2.view

import io.reactivex.Observable
import za.org.grassroot2.model.UserProfile


interface MeView : FragmentView {

    fun displayUserData(profile: UserProfile)

    fun cameraForResult(contentProviderPath: String, s: String)
    fun pickFromGallery()
    fun ensureWriteExteralStoragePermission(): Observable<Boolean>
}