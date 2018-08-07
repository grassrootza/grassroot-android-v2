package za.org.grassroot2.dagger.activity

import android.content.Context
import android.support.v7.app.AppCompatActivity

import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.Module
import dagger.Provides
import za.org.grassroot2.dagger.ActivityContext
import za.org.grassroot2.dagger.ApplicationContext
import za.org.grassroot2.database.DatabaseService
import za.org.grassroot2.model.HomeFeedItem
import za.org.grassroot2.model.Post
import za.org.grassroot2.model.task.VoteResult
import za.org.grassroot2.presenter.ForgottenPasswordPresenter
import za.org.grassroot2.presenter.RegistrationPresenter
import za.org.grassroot2.presenter.activity.GroupDetailsPresenter
import za.org.grassroot2.presenter.activity.GroupSettingsPresenter
import za.org.grassroot2.presenter.activity.PickContactPresenter
import za.org.grassroot2.presenter.fragment.GroupFragmentPresenter
import za.org.grassroot2.presenter.fragment.MemberListPresenter
import za.org.grassroot2.presenter.fragment.SingleTextMultiButtonPresenter

import za.org.grassroot2.services.*
import za.org.grassroot2.services.rest.GrassrootAuthApi
import za.org.grassroot2.services.rest.GrassrootUserApi
import za.org.grassroot2.util.ContactHelper
import za.org.grassroot2.util.ImageUtil
import za.org.grassroot2.util.MediaRecorderWrapper
import za.org.grassroot2.view.adapter.HomeAdapter
import za.org.grassroot2.view.adapter.PostAdapter
import za.org.grassroot2.view.adapter.VoteResultsAdapter

import java.util.*

/**
 * Created by luke on 2017/08/08.
 */
@Module
class ActivityModule(private val act: AppCompatActivity) {

    @Provides
    @ActivityContext
    internal fun activityContext(): AppCompatActivity {
        return act
    }

    @Provides
    @PerActivity
    internal fun providesRxPermission(@ActivityContext act: AppCompatActivity): RxPermissions {
        return RxPermissions(act)
    }

    @Provides
    @PerActivity
    internal fun provideImageUtil(@ActivityContext act: AppCompatActivity): ImageUtil {
        return ImageUtil(act)
    }

    @Provides
    @PerActivity
    internal fun provideMediaService(@ApplicationContext applicationContext: Context, realmService: DatabaseService,
                                     imageUtil: ImageUtil): MediaService {
        return MediaServiceImpl(applicationContext, realmService, imageUtil)
    }

    @Provides
    internal fun provideSingleMultiButtonPresenter(): SingleTextMultiButtonPresenter {
        return SingleTextMultiButtonPresenter()
    }

    @Provides
    @PerActivity

    internal fun provideGroupFragmentPresenter(dbService: DatabaseService, networkService: UserDetailsService, mediaService: MediaService, grassrootUserApi: GrassrootUserApi): GroupFragmentPresenter {
        return GroupFragmentPresenter(dbService, networkService, mediaService, grassrootUserApi)

    }

    @Provides
    @PerActivity
    internal fun provideGroupDetailsPresenter(dbService: DatabaseService, networkService: NetworkService, mediaService: MediaService, grassrootUserApi: GrassrootUserApi): GroupDetailsPresenter {
        return GroupDetailsPresenter(dbService, networkService, mediaService, grassrootUserApi)

    }

    @Provides
    @PerActivity
    internal fun providePickContactPresenter(helper: ContactHelper): PickContactPresenter {
        return PickContactPresenter(helper)
    }

    @Provides
    @PerActivity
    internal fun provideGroupSettingsPresenter(dbService: DatabaseService, networkService: NetworkService): GroupSettingsPresenter {
        return GroupSettingsPresenter(networkService, dbService)
    }

    @Provides
    @PerActivity
    internal fun provideMemberListPresenter(dbService: DatabaseService, networkService: NetworkService): MemberListPresenter {
        return MemberListPresenter(dbService, networkService)
    }

    @Provides
    @PerActivity

    internal fun provideRegistrationPresenter(grassrootAuthApi: GrassrootAuthApi,
                                              userDetailsService: UserDetailsService): RegistrationPresenter {
        return RegistrationPresenter(grassrootAuthApi, userDetailsService)
    }

    @Provides
    @PerActivity
    internal fun provideHomeAdapter(@ActivityContext c: AppCompatActivity): HomeAdapter {
        return HomeAdapter(c, ArrayList<HomeFeedItem>())
    }

    @Provides
    @PerActivity
    internal fun providePostAdapter(@ActivityContext c: AppCompatActivity): PostAdapter {
        return PostAdapter(c, ArrayList<Post>())
    }

    @Provides
    @PerActivity
    internal fun provideVoteResultsAdapter(@ActivityContext c: AppCompatActivity): VoteResultsAdapter {
        return VoteResultsAdapter(c, ArrayList<VoteResult>())
    }

    @Provides
    @PerActivity
    internal fun provideForgottenPasswordPresenter(grassrootAuthApi: GrassrootAuthApi,
                                                   userDetailsService: UserDetailsService): ForgottenPasswordPresenter {
        return ForgottenPasswordPresenter(grassrootAuthApi, userDetailsService)
    }

    @Provides
    @PerActivity
    internal fun providesLocationManager(@ActivityContext act: AppCompatActivity): LocationManager {
        return LocationManager(act)
    }

    @Provides
    @PerActivity
    internal fun providesMediaRecorderWrapper(@ActivityContext act: AppCompatActivity): MediaRecorderWrapper {
        return MediaRecorderWrapper(act)
    }

}
