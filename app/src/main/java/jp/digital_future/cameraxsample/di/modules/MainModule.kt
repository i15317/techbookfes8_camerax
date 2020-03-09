package jp.digital_future.cameraxsample.di.modules

import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import jp.digital_future.cameraxsample.di.PageScope
import jp.digital_future.cameraxsample.di.PerActivityScope
import jp.digital_future.cameraxsample.dispatcher.CameraDispatcher
import jp.digital_future.cameraxsample.ui.camera.CameraFragment
import jp.digital_future.cameraxsample.ui.camera.CameraFragmentModule
import jp.digital_future.cameraxsample.ui.dashboard.DashboardFragment
import jp.digital_future.cameraxsample.ui.home.HomeFragment
import jp.digital_future.cameraxsample.ui.notifications.NotificationsFragment


@Module
internal abstract class MainModule {

    @ContributesAndroidInjector
    abstract fun contributeDashboardFragment(): DashboardFragment

    @ContributesAndroidInjector
    abstract fun contributeHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun contributeNotificationsFragment(): NotificationsFragment

    @PageScope
    @ContributesAndroidInjector(modules = [CameraFragmentModule::class])
    abstract fun contributeCameraFragment(): CameraFragment


}

@Module
internal class MainDispatcherModule {

    @PerActivityScope
    @Provides
    fun provideCameraDispatcher() = CameraDispatcher()

}
