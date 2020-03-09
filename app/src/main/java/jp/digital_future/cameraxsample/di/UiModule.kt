package jp.digital_future.cameraxsample.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.digital_future.cameraxsample.MainActivity
import jp.digital_future.cameraxsample.di.modules.MainDispatcherModule
import jp.digital_future.cameraxsample.di.modules.MainModule

@Module
internal abstract class UiModule {

    @PerActivityScope
    @ContributesAndroidInjector(modules = [MainModule::class, MainDispatcherModule::class])
    internal abstract fun contributeMainActivity(): MainActivity

}