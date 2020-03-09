package jp.digital_future.cameraxsample

import dagger.android.support.DaggerApplication
import jp.digital_future.cameraxsample.di.DaggerAppComponent
import jp.digital_future.cameraxsample.di.applyAutoInjector

import javax.inject.Inject

class App : DaggerApplication() {

    @Inject
    lateinit var appLifecycleCallbacks: AppLifecycleCallbacks

    override fun applicationInjector() = DaggerAppComponent.builder()
        .application(this)
        .build()

    override fun onCreate() {
        super.onCreate()
        applyAutoInjector()
        appLifecycleCallbacks.onCreate(this)
    }

    override fun onTerminate() {
        appLifecycleCallbacks.onTerminate(this)
        super.onTerminate()
    }

}